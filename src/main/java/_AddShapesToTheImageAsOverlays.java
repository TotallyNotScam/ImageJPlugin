import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

@Plugin(type = Command.class, menuPath = "Plugins>Draw>Draw new ROI")
public class _AddShapesToTheImageAsOverlays extends MouseAdapter implements PlugInFilter, KeyListener {
    private static int idCounter;
    private ImagePlus imp;
    private Roi roi;
    private Roi newRoi;
    private Roi temp;
    private boolean isDrawing;
    private boolean finished;
    private boolean isTabDown;
    boolean isCtrlDown;
    private boolean wip;
    private Overlay overlay;
    private ArrayList<Point> points;
    private ArrayList<Point> toSwitch;
    private int[] xPoints;
    private int[] yPoints;
    private int[] xNewPoints;
    private int[] yNewPoints;
    int clickCount;
    private Color[] colors = {Color.red, Color.blue, Color.yellow, Color.green, Color.cyan, Color.magenta, Color.orange, Color.pink};

    @Override
    public void run(ImageProcessor ip) {
        imp = WindowManager.getCurrentImage();
        if (imp.getOverlay() == null) {
            overlay = new Overlay();
        } else {
            overlay = imp.getOverlay();
        }
        if (imp == null) return;
        imp.getCanvas().addMouseListener(this);
        imp.getCanvas().addMouseMotionListener(this);
        imp.getCanvas().addKeyListener(this);
        imp.getCanvas().requestFocus();
        isDrawing = false;
        clickCount = 0;
        finished = false;
        isTabDown = false;
        points = new ArrayList<Point>();
        toSwitch = new ArrayList<Point>();
        String result = OpeningDialog("Choose");
        String[] resultAsArray = result.split("-");
        String id = resultAsArray[0];
        if (!id.contains(" ")) {
            idCounter = Integer.parseInt(id);
            if (overlay.get("layer-" + idCounter) != null) {
                String overwrite = OverwriteExistingLayer("Choose");
                if (overwrite.contains("yes")) {
                    overlay.remove("layer-" + idCounter);
                    finished = false;
                    xPoints = null;
                    yPoints = null;

                } else {
                    finished = true;
                    imp.getCanvas().removeMouseListener(this);
                    imp.getCanvas().removeMouseMotionListener(this);
                }
            }

        }
    }

    public String OpeningDialog(String prompt) {
        String[] choices = {"1-Liver", "2-Heart", "3-Kidney", "4-Brain", "5-Ovary", "6-Hypophysis", "7-Intestine"};
        GenericDialog gd = new GenericDialog("Selector");
        gd.addChoice("Which layer would you like to make?", choices, "1");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return " ";
        }
        return choices[gd.getNextChoiceIndex()];
    }

    public String OverwriteExistingLayer(String prompt) {
        GenericDialog gd = new GenericDialog("Duplication attempt found");
        gd.addRadioButtonGroup("Do you wish to overwrite the existing layer?", new String[]{"Yes", "No"}, 2, 1, "No");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return " ";
        }

        boolean isYes = gd.getNextRadioButton().equals("Yes");
        if (isYes) {
            return "yes";
        } else {
            return "no";
        }
    }

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_ALL;
    }

    public void mousePressed(MouseEvent e) {

        if (!isDrawing && !finished && clickCount == 0) {
            points.clear();
            points.add(new Point(imp.getWindow().getCanvas().offScreenX(e.getX()), imp.getWindow().getCanvas().offScreenY(e.getY())));
            isDrawing = true;
            clickCount++;
            wip = false;
        } else if (roi.contains(imp.getWindow().getCanvas().offScreenX(e.getX()), imp.getWindow().getCanvas().offScreenY(e.getY())) && (isTabDown || isCtrlDown) && finished) {
            toSwitch.clear();
            toSwitch.add(new Point(imp.getWindow().getCanvas().offScreenX(e.getX()), imp.getWindow().getCanvas().offScreenY(e.getY())));
            finished = false;
            wip = true;
        } else {
            imp.getCanvas().repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if ((isCtrlDown || isTabDown) && !finished) {
            toSwitch.add(new Point(imp.getWindow().getCanvas().offScreenX(e.getX()), imp.getWindow().getCanvas().offScreenY(e.getY())));
            xNewPoints = new int[toSwitch.size()];
            yNewPoints = new int[toSwitch.size()];

            for (int i = 0; i < toSwitch.size(); i++) {
                xNewPoints[i] = toSwitch.get(i).x;
                yNewPoints[i] = toSwitch.get(i).y;
            }
            makeRoi(xNewPoints, yNewPoints);
        } else if (isDrawing && !finished && !wip) {
            points.add(new Point(imp.getWindow().getCanvas().offScreenX(e.getX()), imp.getWindow().getCanvas().offScreenY(e.getY())));
            xPoints = new int[points.size()];
            yPoints = new int[points.size()];
            for (int i = 0; i < points.size(); i++) {
                xPoints[i] = points.get(i).x;
                yPoints[i] = points.get(i).y;
            }
            if (points.get(0).equals(points.get(points.size() - 1))) {
                makeRoi(xPoints, yPoints);
            } else {
                Point beginning = points.get(0);
                points.add(beginning);
                xPoints = new int[points.size()];
                yPoints = new int[points.size()];
                for (int i = 0; i < points.size(); i++) {
                    xPoints[i] = points.get(i).x;
                    yPoints[i] = points.get(i).y;
                }
                makeRoi(xPoints, yPoints);
            }
        } else {
            imp.getCanvas().repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if ((isCtrlDown || isTabDown) && !finished && wip) {
            toSwitch.add(new Point(imp.getWindow().getCanvas().offScreenX(e.getX()), imp.getWindow().getCanvas().offScreenY(e.getY())));
            xNewPoints = new int[toSwitch.size()];
            yNewPoints = new int[toSwitch.size()];
            for (int i = 0; i < toSwitch.size(); i++) {
                xNewPoints[i] = toSwitch.get(i).x;
                yNewPoints[i] = toSwitch.get(i).y;
            }
            newRoi = new PolygonRoi(xNewPoints, yNewPoints, toSwitch.size(), Roi.FREEROI);
            imp.setRoi(newRoi);

            wip = true;

        } else if (isDrawing && !finished && !wip) {
            points.add(new Point(imp.getWindow().getCanvas().offScreenX(e.getX()), imp.getWindow().getCanvas().offScreenY(e.getY())));
            xPoints = new int[points.size()];
            yPoints = new int[points.size()];
            for (int i = 0; i < points.size(); i++) {
                xPoints[i] = points.get(i).x;
                yPoints[i] = points.get(i).y;
            }
            roi = new PolygonRoi(xPoints, yPoints, points.size(), Roi.FREELINE);
            imp.setRoi(roi);

        } else {
            imp.getCanvas().repaint();
        }
    }

    public void makeRoi(int[] xPointsP, int[] yPointsP) {
        if (overlay.get("layer-" + idCounter) == null) {
            roi = new PolygonRoi(xPointsP, yPointsP, points.size(), Roi.FREEROI);
        } else if (overlay.get("layer-" + idCounter) != null) {
            temp = new PolygonRoi(xPointsP, yPointsP, toSwitch.size(), Roi.FREEROI);
            Color c = roi.getStrokeColor();
            ShapeRoi s1 = null;
            ShapeRoi s2 = null;
            if (!roi.isArea() && roi.getType() != Roi.POINT) {
                roi = Roi.convertLineToArea(roi);
            }
            if (!temp.isArea() && temp.getType() != Roi.POINT) {
                temp = Roi.convertLineToArea(temp);
            }
            if (roi instanceof ShapeRoi) {
                s1 = (ShapeRoi) roi;
            } else {
                s1 = new ShapeRoi(roi);
            }
            if (temp instanceof ShapeRoi) {
                s2 = (ShapeRoi) temp;
            } else {
                s2 = new ShapeRoi(temp);
            }
            if (isCtrlDown) {
                s1.not(s2);
                overlay.remove(roi.getName());
                overlay.remove(temp);
                overlay.remove(newRoi);
            }
            if (isTabDown) {
                s1.or(s2);
                overlay.remove((roi.getName()));
            }
            roi = s1.trySimplify();
            roi.setStrokeColor(c);

        }
        overlay.remove("layer-" + idCounter);
        roi.setStrokeColor(colors[idCounter]);
        roi.setStrokeWidth(1);
        overlay.add(roi, "layer-" + idCounter);
        imp.setOverlay(overlay);
        imp.setRoi(roi);
        imp.updateAndDraw();
        imp.getCanvas().repaint();
        isDrawing = false;
        finished = true;
        isTabDown = false;
        isCtrlDown = false;
        wip = false;

    }


    @Override
    public void keyTyped(KeyEvent e) {
        //unnecessary
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            isTabDown = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            isCtrlDown = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            isTabDown = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            isCtrlDown = false;
        }
    }
}