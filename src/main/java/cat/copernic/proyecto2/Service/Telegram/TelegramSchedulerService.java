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

    @Autowired(required = false)
    private TelegramBotService botService;

    // Se ejecuta cada hora para buscar citas que ocurran dentro de 24h
    @Scheduled(cron = "0 0 * * * *")
    public void enviarRecordatoriosCitas() {
        LocalDateTime mañanaInicio = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0);
        LocalDateTime mañanaFin = mañanaInicio.plusHours(1);
        List<Cita> citas = citaDAO.findByFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
                mañanaInicio, mañanaFin, EstadoCita.ANULADA);
        for (Cita cita : citas) {
            Long chatId = cita.getPaciente().getUsuario().getTelegramChatId();
            if (chatId != null) {
                String hora = cita.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm"));
                String msj = "⏰ <b>RECORDATORIO</b>\n\nHola " + cita.getPaciente().getNombre() +
                        ", mañana tienes cita a las " + hora + " en " + cita.getEspecialidad() + ".";
                botService.enviarMensaje(chatId, msj);
            }
        }
    }
}