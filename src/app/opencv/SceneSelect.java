package app.opencv;

import app.gui.UserInterface;

/**
 * Zuständig für die Auswahl der passenden 3D-Szene basierend auf der Geste, die auf dem Eingagbebild
 * erkannt wird. Das Erkennen der Handgeste soll später mit den Convexity Defects und k-Nearest-Neighbors
 * erkannt werden.
 * Momentan befinden sich hier placeholder Methoden, die die Szene basieren auf der Farbe des Pixels an der
 * Stelle 0, 0 auswählen. Bei rot wird Szene 0, bei grün Szene 1 und bei blau Szene 2 angezeigt. Die placeholder
 * Methoden sollen später noch ersetzt werden.
 */
public class SceneSelect {

    UserInterface userInterface;

    /**
     * Konstruktor intialisiert die Instanzvariable.
     * @param userInterface	Referenz zum UserInterface
     */
    public SceneSelect(UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    /**
     * TODO: selectScene mit richtiger Szenen-Auswahl ersetzen basierend auf den Convexity Defects.
     * Wählt aus, welcher der fünf 3D-Szenen angezeigt werden soll, basierend auf dem Farbwert an der
     * Stelle 0, 0.
     * @param image Eingabebild
     */
    public void selectScene(int defectsListSize, double rotAspectRatio) {
        if (rotAspectRatio >= 0.7 && rotAspectRatio <= 1.2) {
            System.out.print("Erkannte Geste: Y");
            setScene(0);
        } else if (rotAspectRatio >= 1.5 && rotAspectRatio <= 2) {
            System.out.print("Erkannte Geste: Q");
            setScene(1);
        } else if (rotAspectRatio >= 0.4 && rotAspectRatio <= 0.55 && (defectsListSize / 4) >= 20
                && (defectsListSize / 4) <= 29) {
            System.out.print("Erkannte Geste: K");
            setScene(2);
        } else if (rotAspectRatio >= 0.4 && rotAspectRatio <= 0.55 && (defectsListSize / 4) >= 30
                && (defectsListSize / 4) <= 45) {
            System.out.print("Erkannte Geste: B");
            setScene(3);
        } else if (rotAspectRatio >= 0.8 && rotAspectRatio <= 1) {
            System.out.print("Erkannte Geste: A");
            setScene(4);
        } else {
            System.out.println("Geste konnte nicht erkannt werden.");
        }
    }

    /**
     * Ändert das User Interface zu der OpenGL-Ansicht und zeigt die angegebene Szene an.
     * @param scene Szene die angezeigt werden soll
     */
    public void setScene(int scene) {
        userInterface.setContentScene(scene);
        userInterface.setControlsScene();
    }
}
