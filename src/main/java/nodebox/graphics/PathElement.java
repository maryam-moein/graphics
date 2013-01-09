package nodebox.graphics;

import com.google.common.base.Objects;

import static nodebox.graphics.Point.ZERO;

public class PathElement {

    public static final PathElement CLOSE_ELEMENT = new PathElement(Command.CLOSE, ZERO, ZERO, ZERO);
    public final Command command;
    public final Point point;
    public final Point control1;
    public final Point control2;

    private PathElement(Command command, Point point, Point control1, Point control2) {
        this.command = command;
        this.point = point;
        this.control1 = control1;
        this.control2 = control2;
    }

    public static PathElement moveToCommand(Point point) {
        return new PathElement(Command.MOVE_TO, point, ZERO, ZERO);
    }

    public static PathElement moveToCommand(double x, double y) {
        return new PathElement(Command.MOVE_TO, new Point(x, y), ZERO, ZERO);
    }

    public static PathElement lineToCommand(Point point) {
        return new PathElement(Command.LINE_TO, point, ZERO, ZERO);
    }

    public static PathElement lineToCommand(double x, double y) {
        return new PathElement(Command.LINE_TO, new Point(x, y), ZERO, ZERO);
    }

    public static PathElement curveToCommand(double c1x, double c1y, double c2x, double c2y, double x, double y) {
        return new PathElement(Command.CURVE_TO, new Point(x, y), new Point(c1x, c1y), new Point(c2x, c2y));
    }

    public static PathElement closeCommand() {
        return CLOSE_ELEMENT;
    }

    public Command getCommand() {
        return command;
    }

    public Point getPoint() {
        return point;
    }

    public Point getControl1() {
        return control1;
    }

    public Point getControl2() {
        return control2;
    }

    public PathElement translate(double dx, double dy) {
        if (command == Command.CLOSE) {
            return this;
        } else {
            Point newPoint = this.point.translate(dx, dy);
            Point newControl1 = this.control1;
            Point newControl2 = this.control2;
            if (command == Command.CURVE_TO) {
                newControl1 = this.control1.translate(dx, dy);
                newControl2 = this.control2.translate(dx, dy);
            }
            return new PathElement(command, newPoint, newControl1, newControl2);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(command, point, control1, control2);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof PathElement)) return false;

        final PathElement other = (PathElement) o;
        return Objects.equal(command, other.command)
                && Objects.equal(point, other.point)
                && Objects.equal(control1, other.control2)
                && Objects.equal(control2, other.control2);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("command", command.name())
                .add("point", point)
                .add("control1", control1)
                .add("control2", control2)
                .toString();
    }

    public enum Command {MOVE_TO, LINE_TO, CURVE_TO, CLOSE}

}
