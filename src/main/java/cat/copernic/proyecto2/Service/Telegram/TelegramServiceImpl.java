package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import cat.copernic.proyecto2.Domain.PersonalSanitario.Tratamiento;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.Paciente;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Service.Cita.CitaService;
import cat.copernic.proyecto2.Service.Facturacion.FacturaService;
import cat.copernic.proyecto2.Service.Paciente.PacienteService;
import cat.copernic.proyecto2.DAO.JPA.TratamientoDAO;
import cat.copernic.proyecto2.Utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TelegramServiceImpl implements TelegramService {

    @Autowired
    private PacienteService pacienteService;
    @Autowired
    private CitaService citaService;
    @Autowired
    private FacturaService facturaService;
    @Autowired
    private TratamientoDAO tratamientoDAO;

    @Override
    public boolean soporte(String mensaje) {
        String msg = mensaje.toLowerCase();
        return msg.contains("perfil") || msg.contains("citas") ||
                msg.contains("facturas") || msg.contains("tratamiento") || msg.contains("hola");
    }

    @Override
    public String descripcion() {
        return "Acceso a tu información médica (Perfil, Citas, Facturas, Tratamientos)";
    }

    @Override
    public String rolPermitido() {
        return "PACIENTE";
    }

    @Override
    public String ejecutar(Update update, Usuario usuario) {
        String mensaje = update.getMessage().getText().toLowerCase();
        Paciente paciente = pacienteService.buscarPorUsername(usuario.getUsername());
        if (paciente == null) {
            return "❌ No se ha encontrado un perfil de paciente vinculado a tu cuenta.";
        }
        if (mensaje.contains("perfil")) {
            return verPerfil(paciente);
        } else if (mensaje.contains("citas")) {
            return verCitas(paciente);
        } else if (mensaje.contains("facturas")) {
            return procesarConsultaFacturas(paciente, update.getMessage().getText());
        } else if (mensaje.contains("tratamiento")) {
            return verTratamientos(paciente);
        }
        return "👋 Hola " + paciente.getNombre() + ". Comandos disponibles:\n" +
                "👤 *Perfil*\n📅 *Citas*\n🧾 *Facturas*\n💊 *Tratamiento*";
    }

    private String verPerfil(Paciente p) {
        return "👤 *TU PERFIL*\n" +
                "🆔 DNI: " + p.getDni() + "\n" +
                "👤 Nombre: " + p.getNombre() + " " + p.getApellidos() + "\n" +
                "📧 Email: " + p.getEmail() + "\n" +
                "📞 Teléfono: " + p.getTelefono();
    }

    private String verCitas(Paciente p) {
        List<Cita> citas = citaService.findByPaciente(p);
        if (citas == null || citas.isEmpty())
            return "📅 No tienes citas registradas.";
        StringBuilder sb = new StringBuilder("📅 *TUS CITAS*\n\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Cita c : citas) {
            sb.append("🗓 ").append(c.getFechaHora().format(fmt))
                    .append("\n🩺 ").append(c.getEspecialidad())
                    .append("\n📌 Estado: ").append(c.getEstado()).append("\n\n");
        }
        return sb.toString();
    }

    private String procesarConsultaFacturas(Paciente p, String textoCompleto) {
        String[] partes = textoCompleto.split(" ");
        if (partes.length < 2) {
            return "🔐 *ACCESO SEGURO*\nPor seguridad, para ver tus facturas introduce el comando seguido de tu clave.\n\n"
                    +
                    "Ejemplo: `facturas clave`";
        }
        String claveIntroducida = partes[1].toUpperCase().trim();
        String dniEnBD = p.getDni().toUpperCase().trim();
        try {
            String testigo = CryptoUtils.encrypt(dniEnBD, dniEnBD);
            String resultado = CryptoUtils.decrypt(testigo, claveIntroducida);
            if (!resultado.equals(dniEnBD)) {
                throw new Exception("Clave no coincide");
            }
        } catch (Exception e) {
            return "❌ *Clave incorrecta*. No tienes permiso para descifrar esta información.";
        }
        List<Factura> facturas = facturaService.findByPaciente(p);
        if (facturas == null || facturas.isEmpty())
            return "🧾 No tienes facturas registradas.";
        StringBuilder sb = new StringBuilder("🧾 *FACTURAS DESCIFRADAS*\n\n");
        try {
            for (Factura f : facturas) {
                String totalOriginal = f.getTotal().toString();
                String totalCifrado = CryptoUtils.encrypt(totalOriginal, claveIntroducida);
                String totalDescifrado = CryptoUtils.decrypt(totalCifrado, claveIntroducida);
                sb.append("📄 Nº: ").append(f.getId())
                        .append("\n💰 Importe: ").append(totalDescifrado).append("€")
                        .append("\n📅 Fecha: ").append(f.getFechaEmision()).append("\n\n");
            }
        } catch (Exception e) {
            return "❌ Error crítico al descifrar los datos financieros.";
        }
        return sb.toString();
    }

    private String verTratamientos(Paciente p) {
        List<Tratamiento> tratamientos = tratamientoDAO.findByPacienteIdOrderByFechaInicioDesc(p.getId());
        if (tratamientos == null || tratamientos.isEmpty())
            return "💊 No tienes tratamientos registrados.";
        StringBuilder sb = new StringBuilder("💊 *TRATAMIENTOS*\n\n");
        LocalDate hoy = LocalDate.now();
        for (Tratamiento t : tratamientos) {
            String icono = (t.getFechaFin() != null && !t.getFechaFin().isBefore(hoy)) ? "✅ [ACTUAL]" : "📚 [ANTIGUO]";
            sb.append(icono).append("\n📝 ").append(t.getDescripcion())
                    .append("\n🗓️ Inicio: ").append(t.getFechaInicio())
                    .append("\n🗓️ Fin: ").append(t.getFechaFin() != null ? t.getFechaFin() : "Indefinido")
                    .append("\n\n");
        }
        return sb.toString();
    }
}