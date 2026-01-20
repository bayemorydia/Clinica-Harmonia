package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.DAO.JPA.CitaDAO;
import cat.copernic.proyecto2.DAO.JPA.FacturaDAO;
import cat.copernic.proyecto2.Domain.Persona.Usuario;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Facturacion.Factura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.List;

@Component
public class AccionPaciente implements TelegramAccion {

    @Autowired
    private CitaDAO citaDAO;

    @Autowired
    private FacturaDAO facturaDAO;

    @Override
    public boolean soporte(String mensaje) {
        String m = mensaje.toLowerCase().trim();
        // Ahora aceptará tanto el comando formal como el texto natural
        return m.equals("/mis_citas") || m.contains("mis citas") || m.equals("/facturas") || m.contains("facturas");
    }

    @Override
    public String ejecutar(Update update, Usuario usuario) {
        if (usuario.getPaciente() == null) {
            return "No tienes un perfil de paciente vinculado.";
        }

        String cmd = update.getMessage().getText();

        if (cmd.equals("/mis_citas")) {
            // Usamos el método de búsqueda por paciente de tu CitaDAO
            List<Cita> citas = citaDAO.findByPaciente(usuario.getPaciente());
            if (citas.isEmpty())
                return "No tienes citas programadas.";

            StringBuilder sb = new StringBuilder("📅 Tus Citas:\n");
            citas.forEach(c -> sb.append("- ")
                    .append(c.getFechaHora())
                    .append(" (")
                    .append(c.getEspecialidad())
                    .append(")\n"));
            return sb.toString();
        }

        if (cmd.equals("/facturas")) {
            // Buscamos las facturas asociadas al paciente
            List<Factura> facturas = facturaDAO.findByPaciente(usuario.getPaciente());
            if (facturas.isEmpty())
                return "No tienes facturas registradas.";

            StringBuilder sb = new StringBuilder("🧾 Tus Facturas:\n");
            facturas.forEach(f -> sb.append("- ID: ")
                    .append(f.getId())
                    .append(" | Total: ")
                    .append(f.getTotal())
                    .append("€ | Fecha: ")
                    .append(f.getFechaEmision())
                    .append("\n"));
            return sb.toString();
        }

        return "Comando no reconocido para paciente.";
    }

    @Override
    public String rolPermitido() {
        return "PACIENTE";
    }

    @Override
    public String descripcion() {
        return "Consulta tus citas y facturas médicas";
    }
}