package nodebox.graphics;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.LinkedList;

public class Transform {

    public static final Transform IDENTITY = new Transform(new AffineTransform());
    // The affine transform is mutable, so we have to be careful not to "leak" it.
    private final AffineTransform affineTransform;

    private Transform(AffineTransform t) {
        affineTransform = t;
    }

    public static Transform translateTransform(double tx, double ty) {
        return IDENTITY.translate(tx, ty);
    }

    public Transform concatenate(Transform t) {
        AffineTransform at = (AffineTransform) affineTransform.clone();
        at.concatenate(t.affineTransform);
        return new Transform(at);
    }

    public Transform translate(double tx, double ty) {
        AffineTransform t = (AffineTransform) affineTransform.clone();
        t.translate(tx, ty);
        return new Transform(t);
    }

    public Transform scale(double s) {
        return scale(s, s);
    }

    public Transform scale(double sx, double sy) {
        AffineTransform t = (AffineTransform) affineTransform.clone();
        t.scale(sx, sy);
        return new Transform(t);
    }

    public AffineTransform getAffineTransform() {
        return (AffineTransform) affineTransform.clone();
    }

    public Point map(Point p) {
        double[] in = new double[]{p.x, p.y};
        double[] out = new double[2];
        affineTransform.transform(in, 0, out, 0, 1);
        return new Point(out[0], out[1]);
    }

    public Path map(Path p) {
        GeneralPath newPath = p.toGeneralPath();
        newPath.transform(affineTransform);
        return Path.fromShape(newPath);
    }

    public Group map(Group group) {
        LinkedList<Shape> newShapes = new LinkedList<Shape>();
        for (Shape g : group.getShapes()) {
            newShapes.add((Shape) g.transform(this));
        }
        return Group.of(newShapes);
    }

}
