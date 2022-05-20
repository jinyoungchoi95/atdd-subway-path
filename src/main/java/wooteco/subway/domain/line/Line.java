package wooteco.subway.domain.line;

import java.util.Objects;

public class Line {

    private final Long id;
    private final String name;
    private final String color;
    private final int extraFare;

    public Line(final Long id, final String name, final String color, final int extraFare) {
        validateNegativeExtraFare(extraFare);
        this.id = id;
        this.name = name;
        this.color = color;
        this.extraFare = extraFare;
    }

    private void validateNegativeExtraFare(final int extraFare) {
        if (extraFare < 0) {
            throw new IllegalArgumentException("추가요금은 음수가 들어올 수 없습니다.");
        }
    }

    public Line(final String name, final String color, final int extraFare) {
        this(null, name, color, extraFare);
    }

    public boolean isSameName(final String name) {
        return this.name.equals(name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getExtraFare() {
        return extraFare;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Line line = (Line) o;
        return Objects.equals(id, line.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
