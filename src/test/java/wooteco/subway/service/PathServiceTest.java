package wooteco.subway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import wooteco.subway.dao.line.InmemoryLineDao;
import wooteco.subway.dao.section.InmemorySectionDao;
import wooteco.subway.dao.station.InmemoryStationDao;
import wooteco.subway.domain.line.Line;
import wooteco.subway.domain.path.FindDijkstraShortestPathStrategy;
import wooteco.subway.domain.section.Section;
import wooteco.subway.domain.station.Station;
import wooteco.subway.dto.path.PathFindRequest;
import wooteco.subway.dto.path.PathFindResponse;
import wooteco.subway.exception.NotFoundException;

class PathServiceTest {

    private final InmemoryLineDao lineDao = InmemoryLineDao.getInstance();
    private final InmemorySectionDao sectionDao = InmemorySectionDao.getInstance();
    private final InmemoryStationDao stationDao = InmemoryStationDao.getInstance();
    private PathService pathService = new PathService(sectionDao, stationDao, new FindDijkstraShortestPathStrategy());

    @AfterEach
    void afterEach() {
        lineDao.clear();
        sectionDao.clear();
        stationDao.clear();
    }

    @Test
    @DisplayName("경로를 조회할 수 있다.")
    void findPath() {
        // given
        Line line1 = lineDao.findById(lineDao.save(new Line("1호선", "red", 100)));
        Line line2 = lineDao.findById(lineDao.save(new Line("2호선", "green", 300)));

        Station station1 = stationDao.findById(stationDao.save(new Station("오리")));
        Station station2 = stationDao.findById(stationDao.save(new Station("배카라")));
        Station station3 = stationDao.findById(stationDao.save(new Station("오카라")));
        Station station4 = stationDao.findById(stationDao.save(new Station("레넌")));

        sectionDao.save(new Section(line1, station1, station2, 2));
        sectionDao.save(new Section(line1, station2, station3, 2));
        sectionDao.save(new Section(line2, station1, station4, 3));
        sectionDao.save(new Section(line2, station4, station3, 3));

        // when
        PathFindResponse path = pathService.findPath(new PathFindRequest(station1.getId(), station3.getId(), 15));

        // then
        assertAll(
                () -> assertThat(path.getStations())
                        .extracting("id", "name")
                        .containsExactly(
                                tuple(station1.getId(), station1.getName()),
                                tuple(station2.getId(), station2.getName()),
                                tuple(station3.getId(), station3.getName())
                        ),
                () -> assertThat(path.getDistance()).isEqualTo(4),
                () -> assertThat(path.getFare()).isEqualTo(800)
        );
    }

    @Test
    @DisplayName("찾을 수 없는 경로가 들어오는 경우 예외 발생")
    void findPathExceptionByNotFoundPath() {
        // given
        Line line1 = lineDao.findById(lineDao.save(new Line("1호선", "red", 100)));
        Line line2 = lineDao.findById(lineDao.save(new Line("2호선", "green", 300)));

        Station station1 = stationDao.findById(stationDao.save(new Station("오리")));
        Station station2 = stationDao.findById(stationDao.save(new Station("배카라")));
        Station station3 = stationDao.findById(stationDao.save(new Station("오카라")));
        Station station4 = stationDao.findById(stationDao.save(new Station("레넌")));

        sectionDao.save(new Section(line1, station1, station3, 2));
        sectionDao.save(new Section(line2, station2, station4, 3));

        // when & then
        assertThatThrownBy(() -> pathService.findPath(new PathFindRequest(station1.getId(), station4.getId(), 15)))
                .isInstanceOf(NotFoundException.class);
    }
}
