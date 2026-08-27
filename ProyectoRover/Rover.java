import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

public class Rover {

  public static final double COSTO_DESPLAZAMIENTO = 0.5;
  public static final double COSTO_DETECCION = 0.25;
  public static final double POTENCIA_DEFECTO = 100.0;
  public static final int MAX_RECARGAS = 5;
  public static final double UMBRAL_FUGA = 0.5;

  private static int contadorRovers = 0;
  private static ArrayList<Rover> listaRovers = new ArrayList<>();
  private static final Random GENERADOR_ALEATORIO = new Random();
  private static final DateTimeFormatter FORMATO_FECHA_HORA =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  private String codigo;
  private String nombre;
  private double potenciaInicial;
  private double potenciaDisponible;
  private int posicionInicialX;
  private int posicionInicialY;
  private int posicionActualX;
  private int posicionActualY;
  private int cantidadRecargas;
  private int cantidadDeteccionesCalor;
  private ArrayList<ArrayList<Object>> mandatosExitosos;
  private ArrayList<ArrayList<Object>> mandatosFallidos;

  public Rover(String nombre) {
    this(nombre, POTENCIA_DEFECTO);
  }

  public Rover(String nombre, double potenciaInicial) {
    contadorRovers++;
    this.codigo = String.format("ROV-%02d", contadorRovers);
    this.nombre = nombre;
    this.potenciaInicial = potenciaInicial;
    this.potenciaDisponible = potenciaInicial;
    this.posicionInicialX = 0;
    this.posicionInicialY = 0;
    this.posicionActualX = 0;
    this.posicionActualY = 0;
    this.cantidadRecargas = 0;
    this.cantidadDeteccionesCalor = 0;
    this.mandatosExitosos = new ArrayList<>();
    this.mandatosFallidos = new ArrayList<>();

    listaRovers.add(this);
  }

  public boolean avanzar() {
    return moverse("ADELANTE", 0, 1);
  }

  public boolean retroceder() {
    return moverse("ATRAS", 0, -1);
  }

  public boolean moverDerecha() {
    return moverse("DERECHA", 1, 0);
  }

  public boolean moverIzquierda() {
    return moverse("IZQUIERDA", -1, 0);
  }

  private boolean moverse(String direccion, int deltaX, int deltaY) {
    double potenciaNecesaria = COSTO_DETECCION + COSTO_DESPLAZAMIENTO;
    if (!hayPotenciaSuficiente(potenciaNecesaria)) {
      registrarMandato(direccion, false);
      return false;
    }

    boolean fugaDetectada = detectarFugaCalor();
    if (fugaDetectada) {
      registrarMandato(direccion, false);
      return false;
    }

    posicionActualX += deltaX;
    posicionActualY += deltaY;
    consumirPotencia(COSTO_DESPLAZAMIENTO);
    registrarMandato(direccion, true);
    return true;
  }

  private boolean detectarFugaCalor() {
    consumirPotencia(COSTO_DETECCION);
    cantidadDeteccionesCalor++;
    double numeroAleatorio = GENERADOR_ALEATORIO.nextDouble();
    return numeroAleatorio >= UMBRAL_FUGA;
  }

  private boolean hayPotenciaSuficiente(double cantidadNecesaria) {
    return potenciaDisponible >= cantidadNecesaria;
  }

  private void consumirPotencia(double cantidad) {
    potenciaDisponible -= cantidad;
  }

  private void registrarMandato(String tipo, boolean exitoso) {
    ArrayList<Object> mandato = new ArrayList<>();
    mandato.add(LocalDateTime.now().format(FORMATO_FECHA_HORA));
    mandato.add(tipo);
    mandato.add(exitoso ? "EXITOSO" : "NO POSIBLE");

    if (exitoso) {
      mandatosExitosos.add(mandato);
    } else {
      mandatosFallidos.add(mandato);
    }
  }

  public boolean recargarPotencia(double unidades) {
    if (cantidadRecargas >= MAX_RECARGAS) {
      registrarMandato("RECARGA", false);
      return false;
    }

    potenciaDisponible += unidades;
    cantidadRecargas++;
    registrarMandato("RECARGA", true);
    return true;
  }

  public int[] consultarPosicionActual() {
    return new int[] {posicionActualX, posicionActualY};
  }

  public double consultarPotenciaDisponible() {
    return potenciaDisponible;
  }

  public String consultarEstado() {
    StringBuilder estado = new StringBuilder();
    estado.append("Código: ").append(codigo).append("\n");
    estado.append("Nombre: ").append(nombre).append("\n");
    estado.append("Potencia inicial: ").append(potenciaInicial).append("\n");
    estado.append("Potencia disponible: ").append(potenciaDisponible).append("\n");
    estado.append("Recargas realizadas: ").append(cantidadRecargas)
        .append(" de ").append(MAX_RECARGAS).append("\n");
    estado.append("Detecciones de calor realizadas: ")
        .append(cantidadDeteccionesCalor).append("\n");
    estado.append("Posición inicial: (").append(posicionInicialX)
        .append(", ").append(posicionInicialY).append(")\n");
    estado.append("Posición actual: (").append(posicionActualX)
        .append(", ").append(posicionActualY).append(")\n");
    estado.append("Mandatos exitosos: ").append(mandatosExitosos.size()).append("\n");
    estado.append("Mandatos no posibles: ").append(mandatosFallidos.size());
    return estado.toString();
  }

  public ArrayList<ArrayList<Object>> getMandatosExitosos() {
    return mandatosExitosos;
  }

  public ArrayList<ArrayList<Object>> getMandatosFallidos() {
    return mandatosFallidos;
  }

  public static int getCantidadRoversCreados() {
    return contadorRovers;
  }

  public static ArrayList<Rover> getTodosLosRovers() {
    return listaRovers;
  }
}
