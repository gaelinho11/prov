package com.example.jetpackapploginmvvm.viewmodel
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.remote.creation.compose.state.sqrt
import androidx.compose.remote.creation.toFloat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackapploginmvvm.R
import com.example.jetpackapploginmvvm.model.Baraja
import com.example.jetpackapploginmvvm.model.Carta
import com.example.jetpackapploginmvvm.model.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class BlackjackViewModel(application: Application, private val dao: UserDao, private val username: String) : AndroidViewModel(application),
    SensorEventListener {
    data class BlackjackUiState(
        val cartesJugador: List<Carta> = emptyList(),
        val cartesBanca: List<Carta> = emptyList(),
        val puntuacioJugador: Int = 0,
        val puntuacioBanca: Int = 0,
        val missatge: String = "Vols demanar una carta?",
        val tornJugador: Boolean = true, //per saber quan li toca jugar a la banca, quan es planta el jugador
        val jocAcabat: Boolean = false,

        val dinersTotals: Int = 0,
        val apostaActual: Int = 0,
        val apostaFeta: Boolean = false,

        val segonsPartida: Int = 0,
        val jocEnPausa: Boolean = false
    )

    private val _uiState = MutableStateFlow(BlackjackUiState())
    val uiState = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null //inicialitzo mediaplayer per la musica
    private var soundPool: SoundPool? = null //i soundpool per els efectes
    private var soCartaId: Int = 0
    private var soGuanyarId: Int = 0
    private var soPerdreId: Int = 0

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    //i aquestes per calcular el shake
    private var acceleration = 0f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var lastAcceleration = SensorManager.GRAVITY_EARTH

    private var cronometreJob: Job? = null //crono per demostrar que es pausa quan s'executa en segon pla

    init {
        inicialitzarAudio()//inicialitzo el audio al init
        carregarDiners()//carrego la informacio dels diners al init

        // registro el sensor quan es crea el viewmodel
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun carregarDiners() {
        viewModelScope.launch(Dispatchers.IO) {
            // primer agafo l'usuari
            val user = dao.getUser(username)
            _uiState.update { it.copy(dinersTotals = user?.diners ?: 1000) }
        }
    }
    fun ferAposta(quantitat: Int) {
        if (_uiState.value.dinersTotals >= quantitat) {
            _uiState.update { it.copy(
                apostaActual = quantitat,
                apostaFeta = true,
                missatge = "Aposta de $quantitat€. Bona sort!"
            ) }
        }
    }
    private fun actualitzarDinersBD(nouTotal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = dao.getUser(username)
            user?.let {
                dao.update(it.copy(diners = nouTotal))
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(dinersTotals = nouTotal) }
            }
        }
    }

    private fun inicialitzarAudio() {
        val context = getApplication<Application>()

        // Música de fons (MediaPlayer)[cite: 5]
        mediaPlayer = MediaPlayer.create(context, R.raw.musica_fons) //agafo l'arxiu de la musica
        mediaPlayer?.isLooping = true //poso que es repeteixi
        mediaPlayer?.start()//inicio

        // efectes
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attrs).build()

        // carrego el so de repartir carta
        soCartaId = soundPool?.load(context, R.raw.repartir, 1) ?: 0
        soGuanyarId = soundPool?.load(context, R.raw.guanyar, 1) ?: 0
        soPerdreId = soundPool?.load(context, R.raw.perdre, 1) ?: 0
    }

    //per reproduir els sons
    private fun dispararEfecte(soId: Int) {
        if (soId != 0) {
            soundPool?.play(soId, 1f, 1f, 0, 0, 1f)
        }
    }

    //allibero memoria per evitar memoryleaks
    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        soundPool?.release()
        sensorManager.unregisterListener(this)
    }

    private fun calcularPuntuacio(cartes: List<Carta>): Int {
        var total = cartes.sumOf { it.valor } //sumo totes les cartes
        var numAsos = cartes.count { it.nom == "As" } //compto els As que hi ha per poder aplicar la logica del as

        // aqui aplico la logica, bàsicament si el jugador es passa de 21 i te algun as resto 10 perque valgui 1
        while (total > 21 && numAsos > 0) {
            total -= 10
            numAsos--
        }
        return total
    }
    fun demanarCarta() {//funcio per quan es demana carte
        if (!_uiState.value.tornJugador || _uiState.value.jocAcabat) return //tanquem si s'ha dit que s'acaba el joc

        dispararEfecte(soCartaId)
        val novaCarta = Baraja.llistaCartes.random() //agafo una carta random de la baraja
        val novesCartes = _uiState.value.cartesJugador + novaCarta //sumo la carta nova a les cartes del jugador
        val suma = novesCartes.sumOf { it.valor } // sumo els valors de les cartes del usuari per veure si s'ha passat despres

        _uiState.update { it.copy( //faig un update al uiState amb les dades noves, aqui si la suma es superior a 21 poso el missatge de que ha perdut i sino demano si vol una altre
            cartesJugador = novesCartes,
            puntuacioJugador = suma,
            missatge = if (suma > 21) {
                dispararEfecte(soPerdreId)
                "T'has passat! Has perdut."
            }else if (suma == 21) {
                "21, et plantes"
            }else {
                "Vols una altra?"
            },
            jocAcabat = suma > 21, // si la suma es més gran que 21 acabo el joc
            tornJugador = suma < 21 // si el jugador no arriba a 21 deixo que segueixi jugant
        ) }
        if (suma == 21) {
            plantarse()
        } else if (suma > 21) {
            dispararEfecte(soPerdreId)
        }
    }

    fun plantarse() {
        if (_uiState.value.jocAcabat) return

        _uiState.update { it.copy(tornJugador = false, missatge = "La banca juga...") } // quan es planta poso el torn del jugador en false

        // faig que jugui la banca en un fil secundari
        viewModelScope.launch(Dispatchers.Default) {
            //eldealer demana fins arribar a 17
            while (calcularPuntuacio(_uiState.value.cartesBanca) < 17) {
                delay(1500) //faig una pausa de 1.5 segons per donarli realisme
                dispararEfecte(soCartaId)
                val novaCarta = Baraja.llistaCartes.random()
                _uiState.update { estat ->
                    val noves = estat.cartesBanca + novaCarta
                    estat.copy(
                        cartesBanca = noves,
                        puntuacioBanca = calcularPuntuacio(noves)
                    )
                }
            }

            determinarGuanyador()
        }
    }
    private fun determinarGuanyador() {
        val estat = _uiState.value
        val pJugador = estat.puntuacioJugador
        val pBanca = estat.puntuacioBanca
        val aposta = estat.apostaActual
        var nousDiners = estat.dinersTotals

        val resultat = when { //poso totes les opcions de guanyador
            pJugador > 21 ->{//he hagut de psoar aquest el primer de tots perque encara que es passo la banca si et passes primer perds
                dispararEfecte(soPerdreId)
                actualitzarDinersBD(nousDiners-aposta)
                "T'has passat! Perds $aposta€."
            }
            pBanca > 21 ->{
                dispararEfecte(soGuanyarId)
                actualitzarDinersBD(nousDiners+aposta)
                "La banca s'ha passat! Guanyes $aposta€."
            }
            pJugador > pBanca ->{
                dispararEfecte(soGuanyarId)
                actualitzarDinersBD(nousDiners+aposta)
                "Has superat la banca! Guanyes $aposta€."
            }
            pJugador == pBanca ->{
                dispararEfecte(soGuanyarId)
                actualitzarDinersBD(nousDiners)
                "Empat! Recuperes l'aposta."
            }
            else -> {
                dispararEfecte(soPerdreId)
                actualitzarDinersBD(nousDiners-aposta)
                "La banca guanya, Perds $aposta€."
            }
        }

        _uiState.update { it.copy(missatge = resultat, jocAcabat = true) } //acabo el joc i passo el resultat

    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            lastAcceleration = currentAcceleration
            // Calculem l'acceleració actual (mòdul del vector)
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            val delta = currentAcceleration - lastAcceleration
            // Filtre per suavitzar el moviment
            acceleration = acceleration * 0.9f + delta

            // Baixem el llindar a 10f per facilitar la prova a l'emulador
            if (acceleration > 10f) {
                val estat = _uiState.value
                // Comprovació de seguretat: que hagi apostat, no hagi acabat i sigui el seu torn
                if (estat.apostaFeta && !estat.jocAcabat && estat.tornJugador) {
                    demanarCarta()
                    // Reiniciem l'acceleració per no demanar 5 cartes de cop
                    acceleration = 0f
                }
            }
        }
    }

    fun iniciarCronometre() {
        cronometreJob?.cancel()
        cronometreJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(segonsPartida = it.segonsPartida + 1) }
            }
        }
    }

    fun pausarJoc() {
        cronometreJob?.cancel()//aturo el temps
        desactivarSensors()//aturo el sensotr
        mediaPlayer?.pause()//paro la musica tmb
        _uiState.update { it.copy(jocEnPausa = true) }
    }

    fun reprendreJoc() {
        _uiState.update { it.copy(jocEnPausa = false) }
        activarSensors()//torno a encenre el sensor
        mediaPlayer?.start()//torno a encenre la musica
        iniciarCronometre()//torno a encenre el temps
    }

    fun formatTemps(segons: Int): String { //per formatar el temps en minuts i segons
        val m = segons / 60
        val s = segons % 60
        return "%02d:%02d".format(m, s)
    }


    fun activarSensors() {
        accelerometer?.let {
            //torno a activar el sensor
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }
    fun desactivarSensors() {
        //deixo d'utilitzar el sensor per quan esta en segon pla
        sensorManager.unregisterListener(this)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}



}