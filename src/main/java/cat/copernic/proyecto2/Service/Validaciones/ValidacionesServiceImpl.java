package cat.copernic.proyecto2.Service.Validaciones;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class ValidacionesServiceImpl implements ValidacionesService {

    @Override
    public void validarDocumentoIdentidad(String documento) {
        if (documento == null || documento.isEmpty()) {
            throw new RuntimeException("El documento de identidad es obligatorio");
        }
        
        Pattern pattern = Pattern.compile("(\\d{8}[A-Z]|[XYZ]\\d{7}[A-Z])");
        if (!pattern.matcher(documento).matches()) {
            throw new RuntimeException("Formato de DNI o NIE inválido");
        }
        
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        String nifParaCalculo = documento;
        
        if (documento.startsWith("X"))
            nifParaCalculo = nifParaCalculo.replace("X", "0");
        else if (documento.startsWith("Y"))
            nifParaCalculo = nifParaCalculo.replace("Y", "1");
        else if (documento.startsWith("Z"))
            nifParaCalculo = nifParaCalculo.replace("Z", "2");
            
        try {
            int numero = Integer.parseInt(nifParaCalculo.substring(0, 8));
            char letraEsperada = letras.charAt(numero % 23);
            char letraProporcionada = documento.charAt(8);
            if (letraEsperada != letraProporcionada) {
                throw new RuntimeException("La letra del DNI/NIE no es correcta.");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error al procesar el número del documento");
        }
    }
}