package wooteco.subway.domain;

import java.util.List;
import java.util.Set;

public class Path {

    private static final int DEFAULT_FARE = 1250;
    private static final int DEFAULT_FARE_DISTANCE = 10;

    private static final int ADDITIONAL_UNIT_FARE = 100;
    private static final int FIRST_ADDITIONAL_UNIT_DISTANCE = 5;
    private static final int OVER_ADDITIONAL_UNIT_DISTANCE = 8;
    private static final int FIRST_ADDITIONAL_FARE_DISTANCE = 50;

    private final List<Station> stations;
    private final Set<Line> usedLines;
    private final int distance;

    public Path(final List<Station> stations, final Set<Line> usedLines, final int distance) {
        this.stations = stations;
        this.usedLines = usedLines;
        this.distance = distance;
    }

    public int calculateFare(final int age) {
        AgeDisCountPolicy disCountPolicy = AgeDisCountPolicy.from(age);
        return disCountPolicy.discountedMoney(calcculateDefaultFare() + mostExpensiveLineFare());
    }

    private int calcculateDefaultFare() {
        if (distance <= DEFAULT_FARE_DISTANCE) {
            return DEFAULT_FARE;
        }
        if (distance <= FIRST_ADDITIONAL_FARE_DISTANCE) {
            return DEFAULT_FARE + calculateFirstAdditionalFare();
        }
        return DEFAULT_FARE + calculateFirstAdditionalMaxFare() + calculateOverAdditionalFare();
    }

    private int calculateOverFare(int distance, int unitDistance) {
        return (int) ((Math.ceil((distance - 1) / unitDistance) + 1) * ADDITIONAL_UNIT_FARE);
    }

    private int calculateFirstAdditionalFare() {
        return calculateOverFare(distance - DEFAULT_FARE_DISTANCE, FIRST_ADDITIONAL_UNIT_DISTANCE);
    }

    private int calculateFirstAdditionalMaxFare() {
        return calculateOverFare(FIRST_ADDITIONAL_FARE_DISTANCE - DEFAULT_FARE_DISTANCE,
                FIRST_ADDITIONAL_UNIT_DISTANCE);
    }

    private int calculateOverAdditionalFare() {
        return calculateOverFare(distance - FIRST_ADDITIONAL_FARE_DISTANCE, OVER_ADDITIONAL_UNIT_DISTANCE);
    }

    private int mostExpensiveLineFare() {
        return usedLines.stream()
                .mapToInt(Line::getExtraFare)
                .max()
                .orElseThrow(() -> new IllegalStateException("최대 추가요금을 찾을 수 없습니다."));
    }

    public List<Station> getStations() {
        return stations;
    }

    public int getDistance() {
        return distance;
    }
}
