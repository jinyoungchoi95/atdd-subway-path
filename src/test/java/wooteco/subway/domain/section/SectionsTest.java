package wooteco.subway.domain.section;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import wooteco.subway.domain.line.Line;
import wooteco.subway.domain.station.Station;

class SectionsTest {

    @Test
    @DisplayName("생성 시 size가 0이면 예외가 발생한다.")
    void createExceptionByEmptySize() {
        assertThatThrownBy(() -> new Sections(new ArrayList<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sections는 크기가 0으로는 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("정렬된 Station을 반환할 수 있다.")
    void calculateSortedStations() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Station station4 = new Station(4L, "배리");
        Sections sections = new Sections(List.of(new Section(3L, line, station3, station4, 4),
                new Section(1L, line, station1, station2, 1),
                new Section(2L, line, station2, station3, 2)));

        // when
        List<Station> stations = sections.calculateSortedStations();

        // then
        assertThat(stations).contains(station1, station2, station3, station4);
    }

    @Test
    @DisplayName("Section을 추가할 때 상행, 하행역을 하나도 포함하지않으면 예외가 발생한다.")
    void addSectionExceptionByNotFoundStation() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Station station4 = new Station(4L, "배리");
        Sections sections = new Sections(List.of(new Section(1L, line, station1, station2, 2)));

        // when & then
        assertThatThrownBy(() -> sections.addSection(new Section(line, station3, station4, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("구간 추가는 기존의 상행역 하행역 중 하나를 포함해야합니다.");
    }

    @Test
    @DisplayName("이미 상행에서 하행으로 갈 수 있는 Section이면 예외가 발생한다.")
    void addSectionExceptionByExistSection() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Sections sections = new Sections(List.of(new Section(1L, line, station1, station2, 2),
                new Section(2L, line, station2, station3, 3)));

        // when & then
        assertThatThrownBy(() -> sections.addSection(new Section(line, station1, station3, 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 상행에서 하행으로 갈 수 있는 구간이 존재합니다.");
    }

    @Test
    @DisplayName("입력된 Section의 하행역이 최상행역과 일치할 경우 단순히 추가만 한다.")
    void addSectionByTopSection() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Section section = new Section(1L, line, station2, station3, 3);
        Section addSection = new Section(2L, line, station1, station2, 4);

        Sections sections = new Sections(List.of(section));
        Sections expectedSections = new Sections(List.of(section, addSection));

        // when
        sections.addSection(addSection);

        // then
        assertThat(sections).isEqualTo(expectedSections);
    }

    @Test
    @DisplayName("입력된 Section의 상행역이 최하행역과 일치할 경우 단순히 추가만 한다.")
    void addSectionByBottomSection() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Section section = new Section(1L, line, station1, station2, 3);
        Section addSection = new Section(2L, line, station2, station3, 4);

        Sections sections = new Sections(List.of(section));
        Sections expectedSections = new Sections(List.of(section, addSection));

        // when
        sections.addSection(addSection);

        // then
        assertThat(sections).isEqualTo(expectedSections);
    }

    @Test
    @DisplayName("상행역이 일치하는 역의 사이에 들어갈 수 있다.")
    void addSectionBetweenEqualsUpStation() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Section addSection = new Section(1L, line, station1, station2, 3);
        Sections sections = new Sections(List.of(new Section(2L, line, station1, station3, 10)));

        Sections expectedSections = new Sections(List.of(addSection,
                new Section(2L, line, station2, station3, 7)));

        // when
        sections.addSection(addSection);

        // when
        assertThat(sections).isEqualTo(expectedSections);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    @DisplayName("상행역과 일치하는 역의 사이에 들어갈 때 더 크거나 같은 길이의 Section이면 예외가 발생한다.")
    void addSectionBetweenEqualsUpStationExceptionByEqualsOrLargerDistance(int distance) {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Section addSection = new Section(1L, line, station1, station2, distance);
        Sections sections = new Sections(List.of(new Section(2L, line, station1, station3, 2)));

        // when & then
        assertThatThrownBy(() -> sections.addSection(addSection))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("기존 길이보다 길거나 같은 구간은 중간에 추가될 수 없습니다.");
    }

    @Test
    @DisplayName("하행역이 일치하는 역의 사이에 들어갈 수 있다.")
    void addSectionBetweenEqualsDownStation() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Section addSection = new Section(1L, line, station2, station3, 3);
        Sections sections = new Sections(List.of(new Section(2L, line, station1, station3, 10)));

        Sections expectedSections = new Sections(List.of(addSection,
                new Section(2L, line, station1, station2, 7)));

        // when
        sections.addSection(addSection);

        // when
        assertThat(sections).isEqualTo(expectedSections);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    @DisplayName("하행역과 일치하는 역의 사이에 들어갈 때 더 크거나 같은 길이의 Section이면 예외가 발생한다.")
    void addSectionBetweenEqualsDownStationExceptionByEqualsOrLargerDistance(int distance) {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Section addSection = new Section(1L, line, station2, station3, distance);
        Sections sections = new Sections(List.of(new Section(2L, line, station1, station3, 2)));

        // when & then
        assertThatThrownBy(() -> sections.addSection(addSection))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("기존 길이보다 길거나 같은 구간은 중간에 추가될 수 없습니다.");
    }

    @Test
    @DisplayName("구간 제거 시 Station이 포함되지 않은 경우 예외가 발생한다.")
    void removeSectionExceptionByNotFoundException() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station removeStation = new Station(3L, "오카라");
        Sections sections = new Sections(List.of(new Section(1L, line, station1, station2, 2)));

        // when & then
        assertThatThrownBy(() -> sections.removeSection(removeStation))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 역은 구간에 포함되어있지 않습니다.");
    }

    @Test
    @DisplayName("구간 제거 시 Section이 하나뿐이면 예외가 발생한다.")
    void removeSectionExceptionByLimitSize() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Section section = new Section(1L, line, station1, station2, 2);
        Sections sections = new Sections(List.of(section));

        // when & then
        assertThatThrownBy(() -> sections.removeSection(station2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("구간이 하나뿐이어서 제거할 수 없습니다.");
    }

    @Test
    @DisplayName("입력된 구간이 최상행이라면 해당 구간만 제거된다.")
    void removeTopSection() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Section topSection = new Section(1L, line, station1, station2, 3);
        Section bottomSection = new Section(2L, line, station2, station3, 4);

        Sections sections = new Sections(List.of(topSection, bottomSection));

        // when
        Section section = sections.removeSection(station1);

        // then
        assertThat(section).isEqualTo(topSection);
    }

    @Test
    @DisplayName("입력된 구간이 최하행이라면 해당 구간만 제거된다.")
    void removeBottomSection() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Section topSection = new Section(1L, line, station1, station2, 3);
        Section bottomSection = new Section(2L, line, station2, station3, 4);

        Sections sections = new Sections(List.of(topSection, bottomSection));

        // when
        Section section = sections.removeSection(station3);

        // then
        assertThat(section).isEqualTo(bottomSection);
    }

    @Test
    @DisplayName("입력된 구간이 중간에 있다면 다른 구간이 해당 구간을 연장한다.")
    void removeMiddleSection() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station topStation = new Station(1L, "오리");
        Station middleStation = new Station(2L, "배카라");
        Station bottomStation = new Station(3L, "오카라");

        Section topSection = new Section(1L, line, topStation, middleStation, 3);
        Section bottomSection = new Section(2L, line, middleStation, bottomStation, 4);
        Sections sections = new Sections(List.of(topSection, bottomSection));

        // when
        sections.removeSection(middleStation);

        // then
        assertThat(sections.getSections()).hasSize(1)
                .extracting(Section::getId, Section::getUpStation, Section::getDownStation, Section::getDistance)
                .containsExactly(
                        tuple(topSection.getId(), topStation, bottomStation, 7)
                );
    }

    @Test
    @DisplayName("station이 포함되어있지 않으면 예외가 발생한다.")
    void checkExistStationsExceptionByNotContain() {
        // given
        Line line = new Line(1L, "2호선", "green", 100);
        Station station1 = new Station(1L, "오리");
        Station station2 = new Station(2L, "배카라");
        Station station3 = new Station(3L, "오카라");
        Station station4 = new Station(3L, "레넌");

        Section section = new Section(1L, line, station1, station2, 3);
        Sections sections = new Sections(List.of(section));

        // when
        assertThatThrownBy(() -> sections.checkExistStations(station3, station4))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("현재 Sections에 존재하지 않는 station입니다.");
    }
}
