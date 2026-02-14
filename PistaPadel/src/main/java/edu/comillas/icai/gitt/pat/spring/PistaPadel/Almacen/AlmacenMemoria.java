package edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Usuario;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

//Como todavía no se usa una BBDD, se opta por crear esta clase
//para almacenar la memoria y los usuarios del proyecto.
//Añado la etiqueta Repository para cuando Reserva controller intente usar @Autowired AlmacenMemoria
@Repository
public class AlmacenMemoria {

    private static final AlmacenMemoria almacen = new AlmacenMemoria();

    public static AlmacenMemoria getAlmacen() {
        return almacen;
    }

    private AlmacenMemoria() { }

    private int nextUsuarioId = 1;
    private int nextPistaId = 1;
    private int nextReservaId = 1;

    //Se opta por usar esto como almacen de datos.

    public final Map<Integer, Usuario> usuariosPorId = new HashMap<>();
    public final Map<String, Integer> idUsuarioPorEmail = new HashMap<>();

    public final Map<Integer, Pista> pistasPorId = new HashMap<>();
    public final Map<String, Integer> idPistaPorNombre = new HashMap<>();

    public final Map<Integer, Reserva> reservasPorId = new HashMap<>();

    public int generarIdUsuario() {
        int id = nextUsuarioId;
        nextUsuarioId++;
        return id;
    }

    public int generarIdPista() {
        int id = nextPistaId;
        nextPistaId++;
        return id;
    }

    public int generarIdReserva() {
        int id = nextReservaId;
        nextReservaId++;
        return id;
    }

    public String normalizarEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    public String normalizarNombre(String nombre) {
        if (nombre == null) return null;
        return nombre.trim().toLowerCase();
    }

    //Metodo que mete la reserva en el mapa "reservasPorId"
    public void guardarReserva(Reserva reserva) {
        reservasPorId.put(reserva.getIdReserva(), reserva);
    }

    //Devuelve todas las reservas guardadas
    public Map<Integer, Reserva> getReservas() {
        return reservasPorId;
    }

    //Busca en el mapa pistasPorId si existe una pista con ese numero
    public Pista buscarPista(Integer id) {
        return pistasPorId.get(id);
    }

    //Acceder a usuarios
    public Map<Integer, Usuario> usuarios = usuariosPorId; // Alias para que funcione mi código anterior

    //Permite coger un usuario para que POST no falle
    public Usuario buscarUsuarioPorToken(String token) {
        if (usuariosPorId.isEmpty()) return null;
        return usuariosPorId.values().iterator().next();
    }
}
