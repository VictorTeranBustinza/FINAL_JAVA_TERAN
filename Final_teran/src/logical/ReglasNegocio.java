
package logical;

import javax.swing.JOptionPane;

public class ReglasNegocio {
    
    public void validarDatos(String sDNI, String sMonto, String sPunto,String sMulta, String sCorreo,String sFecha){
        
        if(sDNI.equals("")||sMonto.equals("")||sPunto.equals("")||sMulta.equals("")||sCorreo.equals("")||sFecha.equals("")){
           JOptionPane.showMessageDialog(null, "No puede dejar espacios en blanco", "Error al enviar registro", JOptionPane.ERROR_MESSAGE);
        }else{
           JOptionPane.showMessageDialog(null, "Se ha enviado el formulario satisfactoriamente!!", "Formulario Enciado", JOptionPane.INFORMATION_MESSAGE); 
            
        }
    }
}
