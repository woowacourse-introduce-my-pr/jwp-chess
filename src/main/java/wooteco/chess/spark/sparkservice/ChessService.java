package wooteco.chess.spark.sparkservice;

import wooteco.chess.domain.board.Board;
import wooteco.chess.domain.board.BoardFactory;
import wooteco.chess.domain.board.Position;
import wooteco.chess.domain.judge.Judge;
import wooteco.chess.domain.piece.Piece;
import wooteco.chess.domain.piece.Team;
import wooteco.chess.spark.dao.BoardDAO;
import wooteco.chess.spark.dto.BoardDTO;
import wooteco.chess.spark.dto.GameStatusDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChessService {

    private static ChessService chessService = new ChessService();

    private ChessService() {
    }

    public static ChessService getInstance() {
        return chessService;
    }

    public Board newGame() throws SQLException {
        Board board = BoardFactory.create();

        writeWholeBoard(board);
        writeCurrentTurn(Team.WHITE);

        return board;
    }

    private void writeWholeBoard(final Board board) throws SQLException {
        BoardDAO boardDAO = BoardDAO.getInstance();

        BoardDTO boardDTO = new BoardDTO();
        for (Position position : Position.positions) {
            Piece piece = board.findPieceOn(position);

            boardDTO.setPosition(position.toString());
            boardDTO.setPiece(piece.toString());

            boardDAO.placePieceOn(boardDTO);
        }
    }

    private void writeCurrentTurn(final Team turn) throws SQLException {
        BoardDAO boardDAO = BoardDAO.getInstance();

        GameStatusDTO gameStatusDTO = new GameStatusDTO();
        gameStatusDTO.setCurrentTeam(turn.toString());

        boardDAO.updateTurn(gameStatusDTO);
    }

    public void move(final Position start, final Position end) throws SQLException {
        checkGameOver();

        Board board = readBoard();
        board.move(start, end);

        writeWholeBoard(board);
        writeCurrentTurn(board.getCurrentTurn());
    }

    public Board readBoard() throws SQLException {
        BoardDAO boardDAO = BoardDAO.getInstance();

        List<BoardDTO> boardDTOs = boardDAO.findAllPieces();
        GameStatusDTO gameStatusDTO = boardDAO.readCurrentTurn();

        Team currentTurn = Team.of(gameStatusDTO.getCurrentTeam());
        return new Board(parsePieceInformation(boardDTOs), currentTurn);
    }

    private Map<Position, Piece> parsePieceInformation(final List<BoardDTO> boardDTOs) {
        return boardDTOs.stream()
                .collect(Collectors.toMap(dto -> Position.of(dto.getPosition()), dto -> Piece.of(dto.getPiece())));
    }

    public double calculateScore(final Team team) throws SQLException {
        Judge judge = new Judge();
        return judge.getScoreByTeam(readBoard(), team);
    }

    public List<Position> findMovablePlaces(final Position start) throws SQLException {
        checkGameOver();
        return readBoard().findMovablePositions(start);
    }

    private void checkGameOver() throws SQLException {
        Judge judge = new Judge();

        if (judge.findWinner(readBoard()).isPresent()) {
            throw new IllegalArgumentException();
        }
    }
}
