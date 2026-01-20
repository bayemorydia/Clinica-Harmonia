package cat.copernic.proyecto2.Service.Telegram;

import cat.copernic.proyecto2.DAO.JPA.CitaDAO;
import cat.copernic.proyecto2.Domain.Recepción.Cita;
import cat.copernic.proyecto2.Domain.Recepción.EstadoCita;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TelegramSchedulerService {

    @Autowired
    private CitaDAO citaDAO;

    @Autowired
    private TelegramBotService botService;

    // Se ejecuta cada hora
    @Scheduled(cron = "0 0 * * * *")
    public void enviarRecordatoriosCitas() {
        // Buscamos citas que ocurran exactamente entre 23 y 24 horas a partir de ahora
        LocalDateTime inicioRango = LocalDateTime.now().plusHours(23);
        LocalDateTime finRango = LocalDateTime.now().plusHours(24);

        List<Cita> citasManana = citaDAO.findByFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
                inicioRango, finRango, EstadoCita.ANULADA); //

        for (Cita cita : citasManana) {
            Long chatId = cita.getPaciente().getUsuario().getTelegramChatId();
            if (chatId != null) {
                String fecha = cita.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm"));
                String mensaje = "⏰ *RECORDATORIO DE CITA*\n\nHola " + cita.getPaciente().getNombre() + 
                                 ", te recordamos que mañana tienes una cita a las " + fecha + 
                                 " para la especialidad de " + cita.getEspecialidad() + ".";
                botService.enviarMensaje(chatId, mensaje);
            }
        }
    }
}