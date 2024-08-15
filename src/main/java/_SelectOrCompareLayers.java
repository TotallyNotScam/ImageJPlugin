import ij.IJ;
import ij.ImagePlus;
import ij.gui.*;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Plugin(type = Command.class, menuPath = "Plugins>Show>Show only specific layers")

//things it needs to do: rename; different color; some kind of overwriting

public class _SelectOrCompareLayers extends MouseAdapter implements PlugIn, KeyListener {
    boolean comparison = false;
    boolean stack = false;
    boolean finished = false;
    ImagePlus finalImage;
    boolean union = false;
    boolean intersection = false;
    ArrayList<Integer> trues = new ArrayList<>();
    ArrayList<Point> toSwitch = new ArrayList<>();
    ArrayList<Point> points = new ArrayList<>();
    int[] xPoints;
    int[] yPoints;
    int[] xNewPoints;
    int[] yNewPoints;
    boolean isShiftDown = false;
    boolean isCtrlDown = false;
    boolean modify = false;
    private Roi roi;
    private Roi newRoi;
    private Roi temp;


    public int[] getLayers(String prompt) {
        GenericDialog gd = new GenericDialog("Select");
        String[] labels = new String[]{"1", "2", "3", "4", "5", "6", "7"};
        int[] values = new int[7];
        int tmp = 0;
        boolean[] defaultValues = new boolean[]{false, false, false, false, false, false, false};
        gd.addCheckboxGroup(1, 7, labels, defaultValues);
        gd.addRadioButtonGroup("Comparison?", new String[]{"Yes", "No"}, 2, 1, "No");
        gd.showDialog();
        if (gd.getNextRadioButton().equals("Yes")) {
            comparison = true;
            gd.addRadioButtonGroup("Do you want to stack the pictures on top of each other? ", new String[]{"Yes", "No"}, 2, 1, "No");
            gd.addMessage("Warning, the stack function only works with ONE selected Overlay! \n " +
                    "If you choose more, than only the layer associated with the smallest number will appear.");
            gd.showDialog();
            if (gd.getNextRadioButton().equals("Yes")) {
                stack = true;
            }
        }
        for (int i = 0; i < 7; i++) {
            boolean b = gd.getNextBoolean();
            if (!b) {
                values[i] = i + 1;
            } else {
                trues.add(tmp, values[i] + 1);
                tmp++;
            }
        }
        return values;
    }

    @Override
    public void run(String s) {
        ImagePlus imp = IJ.getImage();
        //copies the original picture, this ensures, that the original picture will not be modified
        ImagePlus copy = imp.duplicate();
        Overlay overlay = copy.getOverlay();
        int[] input = getLayers("Select the needed layers!");
        //checks if it's a comparison
        if (comparison) {
            OpenDialog od1 = new OpenDialog("Select first image");
            String imagePath1 = od1.getPath();
            if (imagePath1 == null) {
                return;
            }
            imp = IJ.openImage(imagePath1);
            OpenDialog od2 = new OpenDialog("Select second image");
            String imagePath2 = od2.getPath();
            if (imagePath2 == null) {
                return;
            }
            copy = imp.duplicate();
            overlay = copy.getOverlay();
            ImagePlus img2 = IJ.openImage(imagePath2);
            ImagePlus copy2 = img2.duplicate();
            Overlay overlay2 = copy2.getOverlay();
            for (int i = 0; i < input.length; i++) {
                //these check if the required layers are present in the images

                if (overlay.get("layer-" + input[i]) != null && overlay2.get("layer-" + input[i]) != null) {
                    overlay.remove("layer-" + input[i]);
                    overlay2.remove("layer-" + input[i]);
                }
                if (overlay.get("layer-" + input[i]) == null && overlay2.get("layer-" + input[i]) != null) {
                    overlay2.remove("layer-" + input[i]);
                }
                if (overlay.get("layer-" + input[i]) != null && overlay2.get("layer-" + input[i]) == null) {
                    overlay.remove("layer-" + input[i]);
                }
            }
            if (stack) {

                Roi roi = overlay2.get("layer-" + trues.get(0));
                String old = roi.getName();
                roi.setStrokeColor(Color.red);
                roi.setName(old + "-B");
                overlay2.add(roi);

                img2.updateAndDraw();

                Roi roiA = overlay.get("layer-" + trues.get(0));
                String oldA = roiA.getName();
                roiA.setStrokeColor(Color.blue);
                roiA.setName(oldA + "-A");
                overlay.add(roiA);
                imp.updateAndDraw();

                for (int i = 0; i < input.length; i++) {
                    overlay.add(overlay2.get(i));
                }
                copy.setOverlay(overlay);
                for (int i = 0; i < overlay.size(); i++) {
                    if (i != overlay.getIndex("layer-" + trues.get(0) + "-A") && i != overlay.getIndex("layer-" + trues.get(0) + "-B")) {
                        overlay.remove(i);
                    }
                }
                finalImage = copy;
                copy.close();
                finalImage.setOverlay(copy.getOverlay());
                finalImage.updateAndDraw();
                finalImage.show();
                finalImage.getCanvas().addKeyListener(this);
                finalImage.getCanvas().requestFocus();
                finalImage.getCanvas().addMouseListener(this);
                finalImage.getCanvas().addMouseMotionListener(this);
                finished = true;
            } else {
                copy.setOverlay(overlay);
                copy.show();
                img2.setOverlay(overlay2);
                img2.show();
            }
        } else {

            for (int i = 0; i < input.length; i++) {
                if (overlay.get("layer-" + input[i]) != null) {
                    overlay.remove("layer-" + input[i]);
                }
            }
            copy.setOverlay(overlay);
            copy.show();
        }

    }

    public boolean[] getChoiceDialog() {
        GenericDialog gd = new GenericDialog("Which ROI would you like to keep?");
        String[] choices;

        boolean[] values;
        int answerIndex;
        if (union && intersection) {
            values = new boolean[5];
            choices = new String[]{"Keep A (Blue)", "Keep B (Red)", "Keep both (Union)", "Only keep the common part (Intersection)", "Delete both"};
        } else {
            values = new boolean[3];
            choices = new String[]{"Keep A (Blue)", "Keep B (Red)", "Delete both"};
        }
        gd.addChoice("Available actions:", choices, choices[values.length - 1]);
        gd.showDialog();
        answerIndex = gd.getNextChoiceIndex();
        for (int i = 0; i < values.length; i++) {
            if (i == answerIndex) {
                values[i] = true;
            } else {
                values[i] = false;
            }
        }
        return values;
    }

    @Override
    public void mousePressed(MouseEvent e) {

        Overlay finalOverlay = finalImage.getOverlay();
        int[] placeholderX = {1, 2, 3};
        int[] placeholderY = {1, 2, 3};
        Roi a = new PolygonRoi(placeholderX, placeholderY, 3, Roi.FREEROI);
        Roi b = new PolygonRoi(placeholderX, placeholderY, 3, Roi.FREEROI);
        Roi t;
        boolean[] answer;

        boolean exists = false;
        int index = -1;
        if (finished) {
            for (int i = 0; i < finalOverlay.size(); i++) {
                if (finalOverlay.get(i).contains(e.getX(), e.getY())) {
                    a = finalOverlay.get(i);
                    String nameA = a.getName();
                    if (nameA.endsWith("A")) {
                        String[] slices = nameA.split("-");
                        String name = slices[1];
                        if (finalOverlay.get("layer-" + name + "-B") != null) {
                            b = finalOverlay.get("layer-" + name + "-B");
                            exists = true;
                            break;

                        }
                    } else if (nameA.contains("-B")) {
                        String[] slices = nameA.split("-");
                        String name = slices[1];
                        if (finalOverlay.get("layer-" + name + "-A") != null) {
                            b = finalOverlay.get("layer-" + name + "-A");
                            //after this the "a" variable will have the overlay with the suffix "A" and "b" has the one with the "B", to avoid confusion
                            t = a;
                            a = b;
                            b = t;
                            exists = true;
                            break;

                        }
                    }
                }
            }
            if (exists) {
                Point[] pointsA = a.getContainedPoints();
                Point[] pointsB = b.getContainedPoints();
                Set<Point> temp = new HashSet<>();
                Set<Point> setA = new HashSet<Point>(Arrays.asList(pointsA));
                Set<Point> setB = new HashSet<Point>(Arrays.asList(pointsB));
                for (Point i : setA) {
                    if (setB.contains(i)) {
                        temp.add(i);
                    }
                }
                if (!temp.isEmpty()) {
                    union = true;
                    intersection = true;
                }
                answer = getChoiceDialog();
                for (int i = 0; i < answer.length; i++) {
                    if (answer[i]) {
                        index = i;
                        break;
                    }
                }
                if (index == 0) {
                    finalOverlay.remove(b);
                    String newName = a.getName();
                    String[] tempName = newName.split("-");
                    newName = tempName[0] + "-" + tempName[1] + "-Final";
                    a.setName(newName);
                    roi = a;
                    finalOverlay.remove(a);
                    finalOverlay.add(roi);
                    finalImage.getCanvas().repaint();
                } else if (index == 1) {
                    finalOverlay.remove(a);
                    String newName = b.getName();
                    String[] tempName = newName.split("-");
                    newName = tempName[0] + "-" + tempName[1] + "-Final";
                    b.setName(newName);
                    b.setFillColor(null);
                    b.setStrokeWidth(1);
                    roi = b;
                    finalOverlay.remove(b);
                    finalOverlay.add(roi);
                    finalImage.getCanvas().repaint();
                } else if ((index == 2 && answer.length == 3) || (index == 4 && answer.length == 5)) {
                    finalOverlay.remove(a);
                    finalOverlay.remove(b);
                    finalImage.getCanvas().repaint();
                } else if (index == 2 && answer.length == 5) {
                    Roi finalroi = null;
                    ShapeRoi s1 = null;
                    ShapeRoi s2 = null;
                    Roi[] rois = new Roi[]{a, b};
                    for (int i = 0; i < rois.length; i++) {
                        Roi roi = rois[i];
                        if (!roi.isArea() && roi.getType() != Roi.POINT) {
                            roi = Roi.convertLineToArea(roi);
                        }
                        if (s1 == null) {
                            if (roi instanceof ShapeRoi) {
                                s1 = (ShapeRoi) roi.clone();
                            } else {
                                s1 = new ShapeRoi(roi);
                            }
                            if (s1 == null) return;
                        } else {
                            if (roi instanceof ShapeRoi) {
                                s2 = (ShapeRoi) roi;
                            } else {
                                s2 = new ShapeRoi(roi);
                            }
                            if (s2 == null) {
                                continue;
                            }
                            s1.or(s2);
                        }
                    }
                    if (s1 != null) {
                        roi = s1.trySimplify();
                        roi.setStrokeColor(a.getStrokeColor());
                        String newName = a.getName();
                        String[] tempName = newName.split("-");
                        newName = tempName[0] + "-" + tempName[1] + "-Final";
                        roi.setName(newName);
                        finalOverlay.remove(a);
                        finalOverlay.remove(b);
                        finalOverlay.add(roi);
                        finalImage.updateAndDraw();
                        finalImage.getCanvas().repaint();
                    }
                } else if (index == 3) {
                    String newName = a.getName();
                    String[] tempName = newName.split("-");
                    newName = tempName[0] + "-" + tempName[1] + "-Final";
                    Color color = a.getStrokeColor();
                    finalOverlay.remove(a);
                    finalOverlay.remove(b);
                    ShapeRoi s1 = null;
                    ShapeRoi s2 = null;
                    if (!a.isArea() && a.getType() != Roi.POINT) {
                        a = Roi.convertLineToArea(a);
                    }
                    if (!b.isArea() && b.getType() != Roi.POINT) {
                        b = Roi.convertLineToArea(b);
                    }
                    if (a instanceof ShapeRoi) {
                        s1 = (ShapeRoi) a;
                    } else {
                        s1 = new ShapeRoi(a);
                    }
                    if (b instanceof ShapeRoi) {
                        s2 = (ShapeRoi) b;
                    } else {
                        s2 = new ShapeRoi(b);
                    }
                    s1.and(s2);
                    roi = s1.trySimplify();
                    roi.setStrokeColor(color);
                    roi.setName(newName);
                    finalOverlay.add(roi);
                    finalImage.updateAndDraw();
                    finalImage.getCanvas().repaint();
                }
                modify = true;
            }

        }
        if (modify && !isCtrlDown && !isShiftDown) {
            points.clear();
            points.add(new Point(finalImage.getWindow().getCanvas().offScreenX(e.getX()), finalImage.getWindow().getCanvas().offScreenY(e.getY())));
        }
        if (modify && (isCtrlDown || isShiftDown)) {
            toSwitch.clear();
            toSwitch.add(new Point(finalImage.getWindow().getCanvas().offScreenX(e.getX()), finalImage.getWindow().getCanvas().offScreenY(e.getY())));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if ((isShiftDown || isCtrlDown) && modify) {
            toSwitch.add(new Point(finalImage.getWindow().getCanvas().offScreenX(e.getX()), finalImage.getWindow().getCanvas().offScreenY(e.getY())));
            xNewPoints = new int[toSwitch.size()];
            yNewPoints = new int[toSwitch.size()];
            for (int i = 0; i < toSwitch.size(); i++) {
                xNewPoints[i] = toSwitch.get(i).x;
                yNewPoints[i] = toSwitch.get(i).y;
            }
            newRoi = new PolygonRoi(xNewPoints, yNewPoints, toSwitch.size(), Roi.FREEROI);
            finalImage.setRoi(newRoi);
        } else if (modify && finalImage.getOverlay().size() == 0) {
            points.add(new Point(finalImage.getWindow().getCanvas().offScreenX(e.getX()), finalImage.getWindow().getCanvas().offScreenY(e.getY())));
            xPoints = new int[points.size()];
            yPoints = new int[points.size()];
            for (int i = 0; i < points.size(); i++) {
                xPoints[i] = points.get(i).x;
                yPoints[i] = points.get(i).y;
            }
            roi = new PolygonRoi(xPoints, yPoints, points.size(), Roi.FREELINE);
            finalImage.setRoi(roi);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if ((isShiftDown || isCtrlDown) && modify) {
            toSwitch.add(new Point(finalImage.getWindow().getCanvas().offScreenX(e.getX()), finalImage.getWindow().getCanvas().offScreenY(e.getY())));
            xNewPoints = new int[toSwitch.size()];
            yNewPoints = new int[toSwitch.size()];

            for (int i = 0; i < toSwitch.size(); i++) {
                xNewPoints[i] = toSwitch.get(i).x;
                yNewPoints[i] = toSwitch.get(i).y;
            }
            makeRoi(xNewPoints, yNewPoints);
        } else if (modify && finalImage.getOverlay().size() == 0) {
            points.add(new Point(finalImage.getWindow().getCanvas().offScreenX(e.getX()), finalImage.getWindow().getCanvas().offScreenY(e.getY())));
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
        }
    }

    public void makeRoi(int[] xPointsP, int[] yPointsP) {
        if (modify && finalImage.getOverlay().size() == 0) {
            roi = new PolygonRoi(xPointsP, yPointsP, points.size(), Roi.FREEROI);
            roi.setStrokeColor(Color.blue);
        } else if (modify && finalImage.getOverlay().size() > 0 && (isShiftDown || isCtrlDown)) {
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
            if (isShiftDown) {
                s1.or(s2);
                finalImage.getOverlay().remove(roi.getName());
            }
            if (isCtrlDown) {

                s1.not(s2);
                finalImage.getOverlay().remove(roi.getName());
                finalImage.getOverlay().remove(temp);
                finalImage.getOverlay().remove(newRoi);
            }
            roi = s1.trySimplify();
            roi.setStrokeColor(c);

        }

        roi.setStrokeWidth(1);
        //finalImage.getOverlay().remove(roi.getName());
        finalImage.getOverlay().add(roi, "layer-" + trues.get(0) + "-Final");
        finalImage.setOverlay(finalImage.getOverlay());
        finalImage.setRoi(roi);
        finalImage.updateAndDraw();
        finalImage.getCanvas().repaint();
        finished = true;
        isCtrlDown = false;
        isShiftDown = false;

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            isShiftDown = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            isCtrlDown = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            isShiftDown = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            isCtrlDown = false;
        }
    }
}
