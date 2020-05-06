package logical;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.Multa;
import model.Respuesta;

public class Servicio {
    
    public ArrayList<String> getTiposMulta() {
        ArrayList<String> lstTiposMultas = new ArrayList<>();
        try {
            Connection con = Conexion.startConeccion();
            Statement statement = con.createStatement();
            String query = "SELECT desc_tipo_multa FROM tipo_multa ORDER BY desc_tipo_multa";
            ResultSet rs = statement.executeQuery(query);
            
            while(rs.next()) {
                lstTiposMultas.add(rs.getString("desc_tipo_multa"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstTiposMultas;   
    }
           
    public ArrayList<Multa> getMultas (){
        ArrayList<Multa> lstMultas = new ArrayList<>();
        try {
         Connection con = Conexion.startConeccion();
         Statement statement = con.createStatement();
         String query = "SELECT * FROM multa ORDER BY monto DESC";
         ResultSet rs = statement.executeQuery(query);
            
         Multa objMulta;
         while(rs.next()) { // Se ejecuta la misma cantidad de veces que filas tiene la tabla
            objMulta = new Multa();
                int idMulta      = rs.getInt("id_multa");
                String dni       = rs.getString("dni");
                String tipoMulta = rs.getString("tipo_multa");
                Double multa     = rs.getDouble("monto");
                String correo    = rs.getString("correo");
                int punto        = rs.getInt("punto");
                
                objMulta.setIdMulta(idMulta);
                objMulta.setDni(dni);
                objMulta.setMulta(tipoMulta);
                objMulta.setMonto(multa);
                objMulta.setCorreo(correo);
                objMulta.setPunto(punto);
                lstMultas.add(objMulta);
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
        return lstMultas;
    }
    
    public Respuesta validar(Multa multa) {
        Respuesta rpta = new Respuesta();
        rpta.setCodigo(0);
        if(multa.getMonto() > 1000) {
            rpta.setCodigo(1);
            rpta.setMsj("La multa no puede ser mayor a 1000 soles");
            return rpta;
        }
        if(multa.getMulta().equalsIgnoreCase("Pico placa") && multa.getMonto()<  130 | multa.getMonto()> 330) {
            rpta.setCodigo(2);
            rpta.setMsj("La multa para pico y placa tiene que ser de 130 a listo ya 330");
            return rpta;
        }
        
        if(multa.getMulta().equalsIgnoreCase("Alta Velocidad")&& multa.getMonto() < 400 | multa.getMonto() >570) {
            rpta.setCodigo(3);
            rpta.setMsj("La multa para Alta Velocidad tiene que ser de 400 a 570");
            return rpta;
        }
        if(multa.getMulta().equalsIgnoreCase("Luz Roja")&& multa.getMonto()< 130 | multa.getMonto() >250) {
            rpta.setCodigo(4);
            rpta.setMsj("La multa para Luz Roja tiene que ser de 130 a 250");
            return rpta;
        }
        if(multa.getMulta().equalsIgnoreCase("Mal estacionado")&& multa.getMonto() <100 | multa.getMonto() >190) {
            rpta.setCodigo(5);
            rpta.setMsj("La multa para Mal estacionado tiene que ser de 100 a 190");
            return rpta;
        }
        
        if(multa.getMulta().equalsIgnoreCase("Alta Velocidad")&& multa.getPunto()<10 | multa.getPunto()>30) {
            rpta.setCodigo(6);
            rpta.setMsj("Los Puntos deben ser min 10 - max 30");
            return rpta;
        }
        
        if(multa.getMulta().equalsIgnoreCase("Luz Roja")&& multa.getPunto()<3 | multa.getPunto()>14) {
            rpta.setCodigo(7);
            rpta.setMsj("Los Puntos deben ser min 3  - max 14");
            return rpta;
        }
        if(multa.getMulta().equalsIgnoreCase("Mal estacionado")&& multa.getPunto()<15 | multa.getPunto()>30) {
            rpta.setCodigo(8);
            rpta.setMsj("Los Puntos deben ser min 15 - max 30");
            return rpta;
        }
        if(multa.getMulta().equalsIgnoreCase("Pico placa")&& multa.getPunto()<13 | multa.getPunto()>20) {
            rpta.setCodigo(9);
            rpta.setMsj("Los Puntos deben ser min 13 - max 20");
            return rpta;
        }
        
        return rpta;
    }
   public Respuesta insertarMultaProcedure(Multa multa) {
        Respuesta rpta = new Respuesta();
        try {
            Connection con = Conexion.startConeccion();
            String sql = "call insertar_multa(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            CallableStatement callableSt = con.prepareCall(sql);
            // PARAMETROS DE ENTRADA (IN)
            callableSt.setString(1, multa.getDni());
            callableSt.setDate(2, new java.sql.Date(multa.getFecha().getTime()));
            callableSt.setInt(3, multa.getPunto());
            callableSt.setDouble(4, multa.getMonto());
            callableSt.setString(5, multa.getCorreo());
            callableSt.setString(6, multa.getMulta());
            // PARAMETROS DE SALIDA (OUT)
            callableSt.registerOutParameter(7, Types.INTEGER);
            callableSt.registerOutParameter(8, Types.VARCHAR);
            callableSt.registerOutParameter(9, Types.INTEGER);
            //Call Stored Procedure
            callableSt.executeUpdate(); // MANDA EL QUERY A BD
            rpta.setCodigo(callableSt.getInt(7));
            rpta.setMsj(callableSt.getString(8));
            rpta.setIdGenerado(callableSt.getInt(9));
            return rpta;
        } catch (Exception e) {
            e.printStackTrace();
            rpta.setCodigo(-1);
            rpta.setMsj("Hubo un error desconocido");
            rpta.setIdGenerado(-1);
            return rpta;
        }
    }
      
   public Respuesta insertarMulta(Multa multa) {
       Respuesta rpta = new  Respuesta (); 
       try {
            rpta = validar(multa);
            if(rpta.getCodigo() != 0) {
                return rpta;
            }
            int sumapunto=getCantidad(multa.getDni());
            if (sumapunto + multa.getPunto()>=100) {
                rpta.setCodigo(-1);
                rpta.setMsj("supero los 100 puntos");
                return rpta;
            }
            int cantidadMultas = getCantidadMultasByDNI_Fecha(multa.getDni(), multa.getFecha());
            if(cantidadMultas == -1) {
                rpta.setCodigo(-1);
                rpta.setMsj("Hubo un error al contabilizar las multas");
                return rpta;
            }
            int cantidadPuntos = getCantidad(multa.getDni());
            if(cantidadPuntos == -1) {
                rpta.setCodigo(-1);
                rpta.setMsj("Hubo un error al contabilizar los puntos");
                return rpta;
            }
            
            System.err.println("cantidadMultas::: "+cantidadMultas);
            if(cantidadMultas >= 2) {
                rpta.setCodigo(3);
                rpta.setMsj("No se puede registrar mas de 2 multas por día");
                return rpta;
            }
            if(cantidadPuntos + multa.getPunto() >= 50 && cantidadPuntos + multa.getPunto() < 80 ) {
                multa.setMonto(multa.getMonto() - (multa.getMonto() * 0.1) ); // incrementos
            } else if(cantidadPuntos + multa.getPunto() >= 80) {
                multa.setMonto(multa.getMonto() - (multa.getMonto() * 0.2) ); // incrementos
            } else{ //DESCUENTO OJO CON ESTO
                int arrCantMultUltMeses[] = getCantidadMultasUltMesByDNI(multa.getDni());
                if(arrCantMultUltMeses == null) {
                    rpta.setCodigo(4);
                    rpta.setMsj("Hubo un error al calcular los puntos.");
                    return rpta;
                }
                if(arrCantMultUltMeses[1] == 0) { // 0 = cant multas ult mes //// 1 = cant multas ult 2 meses
                    multa.setMonto(multa.getMonto() - (multa.getMonto() * 0.15) );
                } else if(arrCantMultUltMeses[0] == 0) {
                    multa.setMonto(multa.getMonto() - (multa.getMonto() * 0.05) );
                }
            }
            Connection con = Conexion.startConeccion();
            String query = "INSERT INTO `sat`.`multa` (`dni`, `tipo_multa`, `monto`, `correo`, `punto`, `fec_regi`) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, multa.getDni());
            ps.setString(2, multa.getMulta());
            ps.setDouble(3, multa.getMonto());
            ps.setString(4, multa.getCorreo());
            ps.setInt(5, multa.getPunto());
            ps.setDate(6, new java.sql.Date(multa.getFecha().getTime()) );
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            int idGenerado = 0;
            if(rs.next()) {
                idGenerado = rs.getInt(1);
            }
            rpta.setIdGenerado(idGenerado);
                       
            rpta.setCodigo(0); // 0 = no error
            rpta.setMsj("Se registró la multa.");
        } catch (Exception e) {
            e.printStackTrace();
            rpta.setCodigo(-1);
            rpta.setMsj("Hubo un error al registrar la multa.");
        }
        return rpta;
    }
   public Respuesta actualizarMulta(Multa multa) {
        Respuesta rpta = new Respuesta();
        try {
            rpta = validar(multa);
            if(rpta.getCodigo() != 0) {
                return rpta;
            }
            Connection con = Conexion.startConeccion();
            String query = "UPDATE `sat`.`multa` SET tipo_multa = ?, monto = ?, correo = ?, punto = ? WHERE id_multa = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, multa.getMulta());
            ps.setDouble(2, multa.getMonto());
            ps.setString(3, multa.getCorreo());
            ps.setInt(4, multa.getPunto());
            ps.setInt(5, multa.getIdMulta());
            ps.executeUpdate();
            rpta.setCodigo(0); // 0 = no error
            rpta.setMsj("Se actualizó la multa.");
        } catch (Exception e) {
            e.printStackTrace();
            rpta.setCodigo(-1);
            rpta.setMsj("Hubo un error al actualizar la multa.");
        }
        return rpta;
   }
   
    public Respuesta borrarMulta (int idMulta){
        Respuesta rpta = new Respuesta();
        try {
            Connection con = Conexion.startConeccion();
            String query = "DELETE FROM `sat`.`multa` WHERE id_multa = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, idMulta);
            ps.executeUpdate();
            rpta.setCodigo(0); // 0 = no error
            rpta.setMsj("Se borró la multa.");
        } catch (Exception e) {
            e.printStackTrace();
            rpta.setCodigo(-1);
            rpta.setMsj("Hubo un error al borrar la multa.");
        }
        return rpta;
}
public int getCantidadMultasByDNI_Fecha(String dni, Date fecha) {
        try {
            //
            Connection con = Conexion.startConeccion();
            String query = "SELECT COUNT(1) AS cantidad FROM `multa` WHERE dni = ? AND DATE(fec_regi) = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, dni);
            ps.setDate(2, new java.sql.Date(fecha.getTime()));
            System.out.println("QUERY A EJECUTAR: "+ps);
            ResultSet rs = ps.executeQuery();
            int cantidadMultas = -1;
            while(rs.next()) {
                cantidadMultas = rs.getInt("cantidad");
            }
            return cantidadMultas;
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
        
    }
    public int getCantidad(String dni) {
        try {
            Connection con = Conexion.startConeccion();
            String query = "SELECT COALESCE(SUM(punto), 0) AS suma FROM multa WHERE dni = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, dni);
            System.out.println("QUERY A EJECUTAR: "+ps);
            ResultSet rs = ps.executeQuery();
            int sumapunto = -1;
            while(rs.next()) {
                sumapunto = rs.getInt("suma");
            }
            return sumapunto;
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    public int[] getCantidadMultasUltMesByDNI(String dni) {
        try {
            //
            Connection con = Conexion.startConeccion();
            String query = "SELECT (SELECT COUNT(1) FROM `multa` WHERE dni = ? AND fec_Regi BETWEEN DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH) AND CURRENT_DATE) AS ultimo_mes,"
                         + "       (SELECT COUNT(1) FROM `multa` WHERE dni = ? AND fec_Regi BETWEEN DATE_SUB(CURRENT_DATE, INTERVAL 2 MONTH) AND CURRENT_DATE) AS ultimo_dos_meses";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, dni);
            ps.setString(2, dni);
            System.out.println("QUERY A EJECUTAR: "+ps);
            ResultSet rs = ps.executeQuery();
            int array_rpta[] = new int[2];
            while(rs.next()) {
                array_rpta[0] = rs.getInt("ultimo_mes");
                array_rpta[1] = rs.getInt("ultimo_dos_meses");
            }
            return array_rpta;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        }

}

    
