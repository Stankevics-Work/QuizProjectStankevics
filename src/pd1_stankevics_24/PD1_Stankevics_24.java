package pd1_stankevics_24;

/**
 * Programmas galvenā klase, kas satur ieejas punktu (main metodi).
 * <p>
 * Šī klase kalpo kā lietojumprogrammas starta punkts. Tā izmanto
 * Java Swing notikumu apstrādes pavedienu (EventQueue.invokeLater),
 * lai droši inicializētu un parādītu lietotāja grafisko saskarni
 * atsevišķā pavedienā, nodrošinot pareizu GUI darbību.
 *
 * @author armins.stankevics_24
 * @see Interface
 * @see javax.swing.SwingUtilities
 */
public class PD1_Stankevics_24 {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new Interface();
        });
    }
}
