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



  /** Cantidad total de Rovers creados hasta el momento. */
  private static int contadorRovers = 0;

  /** Registro de todos los Rovers creados. */
  private static ArrayList<Rover> listaRovers = new ArrayList<>();

  // Nota de diseño: estos dos campos son mecanismo de implementación (no
  // conceptos del dominio), por eso no aparecen en el diagrama de clases.
  // Se declaran estáticos porque su comportamiento no depende de un Rover
  // en particular.
  private static final Random GENERADOR_ALEATORIO = new Random();
  private static final DateTimeFormatter FORMATO_FECHA_HORA =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  // ===========================================================
  // Atributos de instancia
  // ===========================================================

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

  // ===========================================================
  // Constructores
  // ===========================================================

  /**
   * Crea un Rover con la potencia por omisión ({@link #POTENCIA_DEFECTO}).
   *
   * @param nombre nombre de pila del Rover, por ejemplo "Curiosity".
   */
  public Rover(String nombre) {
    this(nombre, POTENCIA_DEFECTO);
  }

  /**
   * Crea un Rover indicando explícitamente su potencia inicial.
   *
   * @param nombre nombre de pila del Rover.
   * @param potenciaInicial unidades de aleación con las que arranca el Rover.
   */
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

  // ===========================================================
  // Métodos públicos de desplazamiento
  // ===========================================================

  /**
   * Desplaza el Rover una posición hacia adelante (incrementa "y").
   *
   * @return {@code true} si el desplazamiento fue exitoso, {@code false} si
   *     no fue posible (por potencia insuficiente o fuga de calor).
   */
  public boolean avanzar() {
    return moverse("ADELANTE", 0, 1);
  }

  /** Desplaza el Rover una posición hacia atrás (decrementa "y"). */
  public boolean retroceder() {
    return moverse("ATRAS", 0, -1);
  }

  /** Desplaza el Rover una posición hacia la derecha (incrementa "x"). */
  public boolean moverDerecha() {
    return moverse("DERECHA", 1, 0);
  }

  /** Desplaza el Rover una posición hacia la izquierda (decrementa "x"). */
  public boolean moverIzquierda() {
    return moverse("IZQUIERDA", -1, 0);
  }

  // ===========================================================
  // Métodos privados de apoyo
  // ===========================================================

  /**
   * Lógica común a los cuatro desplazamientos: valida potencia, ejecuta la
   * detección de fuga de calor y, si es seguro, mueve al Rover.
   *
   * @param direccion nombre de la dirección, usado para registrar el mandato.
   * @param deltaX cuánto cambia "x" (−1, 0 o 1).
   * @param deltaY cuánto cambia "y" (−1, 0 o 1).
   * @return {@code true} si el desplazamiento se realizó, {@code false} si no.
   */
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

  /**
   * Genera un número aleatorio entre 0 y 1 para determinar si hay una fuga
   * de calor en la dirección del desplazamiento. Cada llamado consume
   * potencia y cuenta como una detección, sin importar el resultado.
   *
   * @return {@code true} si el número generado es mayor o igual a
   *     {@link #UMBRAL_FUGA} (se detectó fuga).
   */
  private boolean detectarFugaCalor() {
    consumirPotencia(COSTO_DETECCION);
    cantidadDeteccionesCalor++;
    double numeroAleatorio = GENERADOR_ALEATORIO.nextDouble();
    return numeroAleatorio >= UMBRAL_FUGA;
  }

  /**
   * Verifica si la potencia disponible alcanza para una cantidad dada.
   *
   * @param cantidadNecesaria unidades de aleación requeridas.
   * @return {@code true} si hay potencia suficiente.
   */
  private boolean hayPotenciaSuficiente(double cantidadNecesaria) {
    return potenciaDisponible >= cantidadNecesaria;
  }

  /**
   * Descuenta unidades de la potencia disponible del Rover.
   *
   * @param cantidad unidades de aleación a consumir.
   */
  private void consumirPotencia(double cantidad) {
    potenciaDisponible -= cantidad;
  }

  /**
   * Registra un mandato recibido, con fecha y hora tomadas del sistema
   * operativo, en la lista de exitosos o de no posibles según corresponda.
   *
   * @param tipo descripción del mandato, por ejemplo "ADELANTE" o "RECARGA".
   * @param exitoso si el mandato pudo llevarse a cabo.
   */
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

  // ===========================================================
  // Recarga de potencia
  // ===========================================================

  /**
   * Recarga potencia al Rover, siempre que no se haya alcanzado el límite de
   * {@link #MAX_RECARGAS} recargas.
   *
   * @param unidades unidades de aleación a recargar.
   * @return {@code true} si la recarga fue posible.
   */
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

  // ===========================================================
  // Métodos de consulta
  // ===========================================================

  /**
   * Indica la posición actual del Rover.
   *
   * @return arreglo de dos posiciones: {@code [x, y]}.
   */
  public int[] consultarPosicionActual() {
    return new int[] {posicionActualX, posicionActualY};
  }

  /**
   * Indica la potencia disponible del Rover en este momento.
   *
   * @return unidades de aleación disponibles.
   */
  public double consultarPotenciaDisponible() {
    return potenciaDisponible;
  }

  /**
   * Genera un resumen legible con toda la información de estado del Rover:
   * código, potencia inicial y disponible, recargas, detecciones de calor,
   * posición inicial y actual, y totales de mandatos.
   *
   * @return texto con el estado completo del Rover.
   */
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

  /**
   * Entrega la lista de mandatos que el Rover pudo llevar a cabo con éxito.
   *
   * @return lista de mandatos exitosos.
   */
  public ArrayList<ArrayList<Object>> getMandatosExitosos() {
    return mandatosExitosos;
  }

  /**
   * Entrega la lista de mandatos que el Rover no pudo llevar a cabo.
   *
   * @return lista de mandatos no posibles.
   */
  public ArrayList<ArrayList<Object>> getMandatosFallidos() {
    return mandatosFallidos;
  }

  // ===========================================================
  // Métodos estáticos de consulta global
  // ===========================================================

  /**
   * Indica cuántos Rovers se han creado en total.
   *
   * @return cantidad total de Rovers creados.
   */
  public static int getCantidadRoversCreados() {
    return contadorRovers;
  }

  /**
   * Entrega la lista de todos los Rovers creados hasta el momento.
   *
   * @return lista de todos los Rovers.
   */
  public static ArrayList<Rover> getTodosLosRovers() {
    return listaRovers;
  }
}
