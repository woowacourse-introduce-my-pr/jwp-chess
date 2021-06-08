package chess.service;

import chess.webdto.ChessGameDto;
import chess.webdto.GameRoomDto;
import chess.webdto.GameRoomListDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static chess.service.TeamFormat.BLACK_TEAM;
import static chess.service.TeamFormat.WHITE_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Transactional
class ChessServiceTest {

    @Autowired
    ChessService chessService;

    GameRoomDto testGameRoom;

    @BeforeEach
    void setUp() {
        chessService.createUser("test", "test");
        testGameRoom = chessService.createGameRoom("test");
    }

    @Test
    void validateUser() {
        final boolean userInfo = chessService.validateUser("test", "test");
        assertThat(userInfo).isTrue();
    }

    @Test
    void loadGameRooms() {
        final GameRoomListDto gameRoomListDto = chessService.loadGameRooms();
        final List<GameRoomDto> gameRooms = gameRoomListDto.getGameRooms();
        final Optional<GameRoomDto> gameRoom = gameRooms.stream()
                .filter(gameRoomDto -> gameRoomDto.getRoomId() == testGameRoom.getRoomId())
                .filter(gameRoomDto -> gameRoomDto.getRoomName().equals(testGameRoom.getRoomName()))
                .findFirst();
        assertThat(gameRoom.isPresent()).isTrue();
    }

    @Test
    void createChessGame() {
        final ChessGameDto chessGameDto = chessService.createChessGame(testGameRoom.getRoomId());
        initialChessGameSettingTest(chessGameDto);
    }

    @Test
    void readChessGame() {
        chessService.createChessGame(testGameRoom.getRoomId());
        final ChessGameDto chessGameDto = chessService.readChessGame(testGameRoom.getRoomId());
        initialChessGameSettingTest(chessGameDto);
    }

    private void initialChessGameSettingTest(final ChessGameDto chessGameDto) {
        assertThat(chessGameDto.getIsPlaying()).isTrue();

        assertThat(chessGameDto.getCurrentTurnTeam()).isEqualTo(WHITE_TEAM.asDtoFormat());

        final Map<String, Double> teamScore = chessGameDto.getTeamScore();
        assertThat(teamScore.get(WHITE_TEAM.asDtoFormat())).isEqualTo(38.0);
        assertThat(teamScore.get(BLACK_TEAM.asDtoFormat())).isEqualTo(38.0);

        final Map<String, Map<String, String>> piecePositionByTeam = chessGameDto.getPiecePositionByTeam();
        final Map<String, String> whiteTeamPiecePosition = piecePositionByTeam.get(WHITE_TEAM.asDtoFormat());
        assertThat(whiteTeamPiecePosition.get("d1")).isEqualTo("Queen");
        final Map<String, String> blackTeamPiecePosition = piecePositionByTeam.get(BLACK_TEAM.asDtoFormat());
        assertThat(blackTeamPiecePosition.get("e8")).isEqualTo("King");

        assertThat(chessGameDto.getRoomNumber()).isInstanceOf(Integer.class);
    }

    @Test
    void move() {
        chessService.createChessGame(testGameRoom.getRoomId());
        final ChessGameDto chessGameDto = chessService.move(testGameRoom.getRoomId(), "e2", "e4");

        final Map<String, Map<String, String>> piecePositionByTeam = chessGameDto.getPiecePositionByTeam();
        final Map<String, String> whiteTeamPiecePosition = piecePositionByTeam.get(WHITE_TEAM.asDtoFormat());

        assertThat(whiteTeamPiecePosition.get("e4")).isEqualTo("Pawn");
        assertThat(whiteTeamPiecePosition.getOrDefault("e2", "Empty")).isEqualTo("Empty");
    }

    @Test
    void deleteChessGame() {
        chessService.createChessGame(testGameRoom.getRoomId());
        chessService.deleteChessGame(testGameRoom.getRoomId());
        assertThatCode(() -> chessService.readChessGame(testGameRoom.getRoomId()))
                .isInstanceOf(Exception.class);
    }
}