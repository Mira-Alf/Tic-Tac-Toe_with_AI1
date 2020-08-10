package tictactoe;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        testStageThree();
    }

    public static void testFillStage() {
        //Stage one
        TicTacToeInterface game = new GameFactory().getGame(GameType.FILL_BOARD_AND_GET_INPUT);
        game.playGame();
    }

    public static void testUservsUser() {
        TicTacToeInterface game = new GameFactory().getGame(GameType.USER_USER);
        game.playGame();
    }

    public static void testMachinevsUser() {
        //Stage two
        TicTacToeInterface game = new GameFactory().getGame(GameType.USER_MACHINE, GameLevel.EASY);
        game.playGame();
    }

    public static void testStageThree() {
        ProgramTemplate template = new ProgramTemplate();
        template.startPlay();
    }
}

class ProgramTemplate {
    private String command;
    private String[] players = new String[2];
    Scanner scanner = new Scanner(System.in);
    private TicTacToeInterface game;
    private boolean isExit = false;

    public void startPlay() {
        while(command == null || !command.equals("exit")) {
            System.out.print("Input command: ");
            String inputLine = scanner.nextLine();
            parseLineIntoInputs(inputLine);
            if (!areInputsValid()) {
                System.out.println("Bad parameters!");
                continue;
            }
            if(!isExit) {
                setGame();
                game.playGame();
            }
        }
    }

    private void parseLineIntoInputs(String inputLine) {
        String[] inputs = inputLine.split("\\s+");
        if(inputs.length >= 1)
            command = inputs[0];
        players[0] = inputs.length >= 2 ? inputs[1] : null;
        players[1] = inputs.length >= 3 ? inputs[2] : null;
    }

    private void setGame() {
        GameFactory factory = new GameFactory();
        switch(players[0]) {
            case "user" :
                game = players[1].equals("user") ? factory.getGame(GameType.USER_USER) :
                        factory.getGame(GameType.USER_MACHINE, factory.getGameLevel(players[1]));
                break;
            default:
                game = players[1].equals("user") ? factory.getGame(GameType.MACHINE_USER,
                        factory.getGameLevel(players[0])) : factory.getGame(GameType.MACHINE_MACHINE,
                        factory.getGameLevel(players[0]), factory.getGameLevel(players[1]));
                break;
        }
    }

    private boolean areInputsValid() {
        boolean result = command != null && (command.equals("start") || command.equals("exit"));
        isExit = command != null && command.equals("exit");
        for(String p : players) {
            result = result && p != null && (p.equals("user") ||
                    p.equals("easy") || p.equals("medium") || p.equals("hard"));
        }
        return result || isExit;
    }
}

enum GameLevel {
    EASY("easy"), MEDIUM("medium"), HARD("hard");
    private String level;

    GameLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }
}

enum GameStatus {
    GAME_NOT_FINISHED("Game not finished"),
        DRAW("Draw"),
        X_WINS("X wins"),
        O_WINS("O wins");
    private String message;

    GameStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}

enum RoundType {
    USER_ROUND, MACHINE_ROUND, USER2_ROUND, FILL_ROUND;
}

enum ElementError {
    CELL_OCCUPIED("This cell is occupied! Choose another one!"),
    NOT_NUMBER("You should enter numbers!"),
    NOT_TWO_INPUTS("You should enter numbers!"),
    NOT_IN_RANGE("Coordinates should be from 1 to "+BoardUtil.BOARD_SIZE+"!"),
    INVALID_BOARD_LENGTH("The board string should not contain more than "+(BoardUtil.BOARD_SIZE*BoardUtil.BOARD_SIZE)),
    BOARD_INVALID_CHAR("Not a valid character on the board - only "+ Arrays.toString(BoardUtil.CHARS_ALLOWED)+" are allowed!"),
    BOARD_INVALID_STATE("The difference between the number of times the symbols occur on the board should not be more than one!");

    private String message;

    ElementError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}

enum GameType {
    FILL_BOARD_AND_GET_INPUT, USER_MACHINE, MACHINE_USER, USER_USER, MACHINE_MACHINE;
}

class GameFactory {

    public GameType getGameType(String[] typeArray) {
        GameType gameType = typeArray[0].equals("user") && typeArray[1].equals("user") ? GameType.USER_USER : null;
        gameType = gameType == null && typeArray[0].equals("user") && !typeArray[1].equals("user") ? GameType.USER_MACHINE : null;
        gameType = gameType == null && !typeArray[0].equals("user") && typeArray[1].equals("user") ? GameType.MACHINE_USER : null;
        gameType = gameType == null && !typeArray[0].equals("user") && !typeArray[1].equals("user") ? GameType.MACHINE_MACHINE : null;
        return gameType;
    }

    public TicTacToeInterface getGame(GameType type) {
        switch(type) {
            case FILL_BOARD_AND_GET_INPUT:
                return new FillBoardAndTestTicTac();
            case USER_USER:
                return new UserVersusUserTicTac();
        }
        return null;
    }

    public TicTacToeInterface getGame(GameType type, GameLevel level) {
        switch(type) {
            case USER_MACHINE:
            case MACHINE_USER:
                return new MachineVersusUserAndOtherwiseTicTac(type, level);
        }
        return null;
    }

    public TicTacToeInterface getGame(GameType type, GameLevel level1, GameLevel level2) {
        switch(type) {
            case MACHINE_MACHINE:
                return new MachineVersusMachineTicTac(level1, level2);
        }
        return null;
    }

    public GameLevel getGameLevel(String level) {
        switch(level) {
            case "easy":
                return GameLevel.EASY;
            case "medium":
                return GameLevel.MEDIUM;
            case "hard":
                return GameLevel.HARD;
        }
        return null;
    }

    public ComputerStrategy getStrategy(GameLevel level) {
        switch(level) {
            case EASY:
                return new EasyGameStrategy();
            case MEDIUM:
                return new MediumGameStrategy();
            case HARD:
                return new HardGameStrategy();

        }
        return null;
    }
}



interface TicTacToeInterface {
    public void playGame();
}

abstract class TicTacToeGame implements TicTacToeInterface {
    protected GameType gameType;
    protected GameStatus gameStatus = GameStatus.GAME_NOT_FINISHED;
    protected List<GameRound> rounds = new ArrayList<>();
    protected BoardUtil boardUtil;
    protected final Scanner scanner = new Scanner(System.in);

    public Scanner getScanner() {
        return scanner;
    }

    public void addRound(GameRound round) {
        rounds.add(round);
    }

    public GameRound getRound() {
        if(rounds.size()>0)
            return rounds.get(rounds.size()-1);
        else
            return null;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public GameStatus getGameStatus() {
        return this.gameStatus;
    }

    public BoardUtil getBoardUtil() {
        return boardUtil;
    }

}

class UserVersusUserTicTac extends TicTacToeGame {
    public UserVersusUserTicTac() {
        this.gameType = GameType.USER_USER;
        this.boardUtil = new BoardUtil(this, scanner);
    }

    @Override
    public void playGame() {
        BoardVerifier boardVerifier = boardUtil.getBoardVerifier();
        boardVerifier.initBoardVerifier();
        boardUtil.displayBoard();
        boolean isInputX = true;

        while(gameStatus == GameStatus.GAME_NOT_FINISHED) {
            char value = isInputX ? BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX] :
                    BoardUtil.CHARS_ALLOWED[BoardUtil.O_INDEX];

            RoundType roundType = isInputX ? RoundType.USER_ROUND : RoundType.USER2_ROUND;
            GameRound round = new GameRound(boardUtil, roundType);
            addRound(round);
            round.getNextMove();
            round.getInput().setValue(value);

            boardUtil.updateBoard(round.getInput(), roundType);
            boardUtil.displayBoard();
            boardUtil.updateGameStatus();
            isInputX = !isInputX;
        }
        System.out.println(gameStatus.getMessage());
    }
}

class MachineVersusUserAndOtherwiseTicTac extends TicTacToeGame {

    private GameLevel level;
    private boolean isFirstPlayUser;
    private ComputerStrategy strategy;

    public MachineVersusUserAndOtherwiseTicTac(GameType gameType, GameLevel level) {
        this.gameType = gameType;
        this.boardUtil = new BoardUtilWithAILogic(this, scanner);
        this.level = level;
        switch(gameType) {
            case USER_MACHINE:
                isFirstPlayUser = true;
                break;
            case MACHINE_USER:
                isFirstPlayUser = false;
                break;
        }
        this.strategy = new GameFactory().getStrategy(level);
    }

    @Override
    public void playGame() {
        BoardVerifier boardVerifier = boardUtil.getBoardVerifier();
        boardVerifier.initBoardVerifier();
        boardUtil.displayBoard();
        boolean isInputX = true;
        boolean isPlayUser = isFirstPlayUser;

        while(gameStatus == GameStatus.GAME_NOT_FINISHED) {
            char value = isInputX ? BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX] :
                    BoardUtil.CHARS_ALLOWED[BoardUtil.O_INDEX];

            RoundType roundType = isPlayUser ? RoundType.USER_ROUND :
                    RoundType.MACHINE_ROUND;
            GameRound round = roundType == RoundType.USER_ROUND ?
                    new GameRound(boardUtil, roundType) :
                    new GameRound(boardUtil, roundType, strategy);
            addRound(round);
            round.getNextMove(value);
            round.getInput().setValue(value);

            boardUtil.updateBoard(round.getInput(), roundType);
            boardUtil.displayBoard();
            boardUtil.updateGameStatus();
            isInputX = !isInputX;
            isPlayUser = !isPlayUser;
        }
        System.out.println(gameStatus.getMessage());

    }
}

class MachineVersusMachineTicTac extends TicTacToeGame {

    private GameLevel[] levels;
    private ComputerStrategy[] strategies;

    public MachineVersusMachineTicTac(GameLevel level1, GameLevel level2) {
        GameFactory factory = new GameFactory();
        this.levels = new GameLevel[]{level1, level2};
        this.strategies = new ComputerStrategy[] { factory.getStrategy(levels[0]), factory.getStrategy(levels[1]) };
        this.gameType = GameType.MACHINE_MACHINE;
        this.boardUtil = new BoardUtilWithAILogic(this, scanner);
    }

    @Override
    public void playGame() {
        BoardVerifier boardVerifier = boardUtil.getBoardVerifier();
        boardVerifier.initBoardVerifier();
        boardUtil.displayBoard();
        boolean isInputX = true;

        while(gameStatus == GameStatus.GAME_NOT_FINISHED) {
            char value = isInputX ? BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX] :
                    BoardUtil.CHARS_ALLOWED[BoardUtil.O_INDEX];
            ComputerStrategy strategy = isInputX ? strategies[0] : strategies[1];

            RoundType roundType = RoundType.MACHINE_ROUND;
            GameRound round = new GameRound(boardUtil, roundType, strategy);
            addRound(round);
            round.getNextMove(value);
            round.getInput().setValue(value);

            boardUtil.updateBoard(round.getInput(), roundType);
            boardUtil.displayBoard();
            boardUtil.updateGameStatus();
            isInputX = !isInputX;
        }
        System.out.println(gameStatus.getMessage());
    }
}

class FillBoardAndTestTicTac extends TicTacToeGame {

    private String boardString;
    private char nextCharAsInput;

    public FillBoardAndTestTicTac() {
        this.gameType = GameType.FILL_BOARD_AND_GET_INPUT;
        this.boardUtil = new FillBoardAndTestBoardUtil(this, scanner);
    }

    public void setBoardString(String boardString) {
        this.boardString = boardString;
    }

    public void setNextCharAsInput(char nextCharAsInput) {
        this.nextCharAsInput = nextCharAsInput;
    }

    public char getNextCharAsInput() {
        return nextCharAsInput;
    }

    @Override
    public void playGame() {

        FillBoardAndTestBoardVerifier boardVerifier = (FillBoardAndTestBoardVerifier)boardUtil.getBoardVerifier();
        boardVerifier.initBoardVerifier();

        boardUtil.displayBoard();

        ElementError error = boardVerifier
                .isBoardStringValid(boardString);
        if(error != null) {
            System.out.println(error.getMessage());
            return;
        }

        boardUtil.updateGameStatus();

        GameRound round = new GameRound(boardUtil, RoundType.FILL_ROUND);
        addRound(round);
        round.getNextMove();
        round.getInput().setValue(nextCharAsInput);

        boardUtil.updateBoard(round.getInput(), RoundType.FILL_ROUND);
        boardUtil.displayBoard();
        boardUtil.updateGameStatus();
        System.out.println(gameStatus.getMessage());

    }

}

class BoardUtil {
    public static final char[] CHARS_ALLOWED = {'_', 'X', 'O'};
    public static final int BOARD_SIZE = 3;
    public static final int EMPTY_SPACE_INDEX = 0;
    public static final int X_INDEX = 1;
    public static final int O_INDEX = 2;

    protected char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
    protected int[] boardStatistics = new int[]{0, 0, 0};
    protected List<Element> emptySpaces = new ArrayList<>();
    protected BoardVerifier boardVerifier;
    protected TicTacToeGame game;

    public BoardUtil() {

    }

    public BoardUtil(BoardUtil boardUtil) {
        this.board = boardUtil.getCopyOfBoard();
        this.boardStatistics = boardUtil.getCopyOfBoardStatistics();
        this.emptySpaces = boardUtil.getCopyOfEmptySpaces();
        this.boardVerifier = new BoardVerifier(this);
        boardVerifier.initBoardVerifier();
    }

    public BoardUtil(TicTacToeGame game, Scanner scanner) {
        this.game = game;
        this.boardVerifier = new BoardVerifier(this);
        initEmptyBoard();
    }

    private int[] getCopyOfBoardStatistics() {
        int[] copyOfBoardStatistics = new int[BOARD_SIZE];
        for(int i = 0; i < BOARD_SIZE; i++) {
            copyOfBoardStatistics[i] = boardStatistics[i];
        }
        return copyOfBoardStatistics;
    }

    private List<Element> getCopyOfEmptySpaces() {
        List<Element> copyOfEmptySpaces = new ArrayList<>();
        for(Element element : emptySpaces) {
            Element e = Element.createElement(element.getRowIndex(), element.getColIndex());
            copyOfEmptySpaces.add(e);
        }
        return copyOfEmptySpaces;
    }

    public void initEmptyBoard() {
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = CHARS_ALLOWED[EMPTY_SPACE_INDEX];
                updateBoardValue(i, j, CHARS_ALLOWED[EMPTY_SPACE_INDEX]);
            }
        }
    }

    public List<Element> getEmptySpaces() {
        return this.emptySpaces;
    }


    public int[] getBoardStatistics() {
        return this.boardStatistics;
    }

    public char[][] getCopyOfBoard() {
        char[][] copyOfBoard = new char[BOARD_SIZE][BOARD_SIZE];
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                copyOfBoard[i][j] = board[i][j];
            }
        }
        return copyOfBoard;
    }

    public void updateBoardValue(int rowIndex, int colIndex, char c) {
        board[rowIndex][colIndex] = c;
        updateBoardStatistics(Element.createElement(rowIndex, colIndex), c);
    }

    public void updateBoardValue(Element element) {
        int rowIndex = element.getRowIndex(), colIndex = element.getColIndex();
        char value = element.getValue();

        updateBoardValue(rowIndex, colIndex, value);
    }

    public void updateBoardStatistics(Element element, char c) {
        switch(c) {
            case 'X':
                boardStatistics[X_INDEX]++;
                removeFromEmptySpacePool(element);
                break;
            case 'O':
                boardStatistics[O_INDEX]++;
                removeFromEmptySpacePool(element);
                break;
            case '_':
                addToEmptySpacesPool(element);
        }
    }

    public void addToEmptySpacesPool(Element element) {
        List<Element> emptySpacesList = emptySpaces.stream()
                .filter(s -> s.getRowIndex() == element.getRowIndex() && s.getColIndex() == element.getColIndex())
                .collect(Collectors.toList());
        Element alreadyPresent = emptySpacesList.size() == 1 ? emptySpacesList.get(0) : null;
        if(alreadyPresent == null) {
            emptySpaces.add(element);
            boardStatistics[EMPTY_SPACE_INDEX]++;
        }
    }

    public void removeFromEmptySpacePool(Element element) {
        List<Element> emptySpacesList = emptySpaces.stream()
                .filter(s -> s.getRowIndex() == element.getRowIndex() && s.getColIndex() == element.getColIndex())
                .collect(Collectors.toList());
        Element alreadyPresent = emptySpacesList.size() == 1 ? emptySpacesList.get(0) : null;
        if(alreadyPresent != null) {
            emptySpaces.remove(alreadyPresent);
            boardStatistics[EMPTY_SPACE_INDEX]--;
        }
    }

    public void displayBoard() {
        System.out.println("---------");
        for(int i = 0; i < BOARD_SIZE; i++) {
            System.out.print("| ");
            for(int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(board[i][j]+" ");
            }
            System.out.println("|");
        }
        System.out.println("---------");
    }

    public void displayBoardStatistics() {
        for( int i = 0; i <= O_INDEX; i++ ) {
            System.out.print(boardStatistics[i]+" ");
        }
        System.out.println();
        emptySpaces.forEach(s -> System.out.println("("+s.getRowIndex()+", "+s.getColIndex()+")"));
    }

    public void updateGameStatus() {
        GameRound round = game.getRound();
        if(round != null)
            game.setGameStatus(boardVerifier.checkRoundInput(round));
    }

    public GameStatus getGameStatus(GameRound round) {
        return boardVerifier.checkRoundInput(round);
    }
    public TicTacToeGame getGame() {
        return game;
    }

    public BoardVerifier getBoardVerifier() {
        return this.boardVerifier;
    }

    public void updateBoard(Element element, RoundType roundType) {
        updateBoardValue(element);
        boardVerifier.updateCopyBoard(element);
    }
}

class BoardUtilWithAILogic extends BoardUtil {

    public BoardUtilWithAILogic() {
        this.boardVerifier = new BoardVerifierWithAILogic(this);
    }

    public BoardUtilWithAILogic(BoardUtilWithAILogic boardUtilWithAILogic) {
        super(boardUtilWithAILogic);
    }

    public BoardUtilWithAILogic(TicTacToeGame game, Scanner scanner) {
        this.game = game;
        this.boardVerifier = new BoardVerifierWithAILogic(this);
        initEmptyBoard();
    }

}

class FillBoardAndTestBoardUtil extends BoardUtil {


    public FillBoardAndTestBoardUtil(FillBoardAndTestTicTac game, Scanner scanner) {
        this.game = game;
        this.boardVerifier = new FillBoardAndTestBoardVerifier(this);
        System.out.print("Enter cells: ");
        String boardString = scanner.nextLine();
        game.setBoardString(boardString);
        initBoardWithString(boardString);
    }

    private void initBoardWithString(String initialState) {
        int rowIndex = 0, colIndex = 0;
        for(char c: initialState.toCharArray()) {
            updateBoardValue(rowIndex, colIndex, c);
            colIndex++;
            if(colIndex == BOARD_SIZE) {
                rowIndex++;
                colIndex = 0;
            }
        }
        updateNextCharAsInput();
    }

    public void updateNextCharAsInput() {
        FillBoardAndTestTicTac fillGame = (FillBoardAndTestTicTac) game;
        char nextCharAsInput = '\u0000';
        if(boardStatistics[X_INDEX] == boardStatistics[O_INDEX])
            nextCharAsInput = CHARS_ALLOWED[X_INDEX];
        else if(boardStatistics[X_INDEX] - boardStatistics[O_INDEX] == 1)
            nextCharAsInput = CHARS_ALLOWED[O_INDEX];
        else if(boardStatistics[O_INDEX] - boardStatistics[X_INDEX] == 1)
            nextCharAsInput = CHARS_ALLOWED[X_INDEX];
        fillGame.setNextCharAsInput(nextCharAsInput);
    }

    @Override
    public void updateGameStatus() {
        FillBoardAndTestBoardVerifier verifier = (FillBoardAndTestBoardVerifier) boardVerifier;
        GameRound round = game.getRound();
        if(round == null) {
            game.setGameStatus(verifier.checkFullBoard());
        } else
            game.setGameStatus(boardVerifier.checkRoundInput(round));
    }

}

class BoardVerifier {
    protected BoardUtil boardUtil;
    protected char[][] copyOfBoard;
    protected Element validInput;

    public BoardVerifier() {

    }

    public BoardVerifier(BoardUtil boardUtil) {
        this.boardUtil = boardUtil;
    }

    protected void displayBoard() {
        for(int i = 0; i < BoardUtil.BOARD_SIZE; i++) {
            for(int j = 0; j < BoardUtil.BOARD_SIZE; j++) {
                System.out.print(copyOfBoard[i][j]+" ");
            }
            System.out.println();
        }
    }

    protected ElementError isInputValid(String inputLine) {
        String[] inputs = inputLine.split("\\s+");
        if(inputs.length != 2)
            return ElementError.NOT_TWO_INPUTS;
        for(int i = 0; i < inputs.length; i++) {
            for(char c: inputs[i].toCharArray()) {
                if(!Character.isDigit(c))
                    return ElementError.NOT_NUMBER;
            }
        }
        int index1 = Integer.parseInt(inputs[0]);
        int index2 = Integer.parseInt(inputs[1]);
        if(index1 <1 || index1>3 || index2<1 || index2>3)
            return ElementError.NOT_IN_RANGE;
        return isCellOccupied(Element.getElement(index1, index2));
    }

    public ElementError isCellOccupied(Element element) {
        if(copyOfBoard[element.getRowIndex()][element.getColIndex()] != '_')
            return ElementError.CELL_OCCUPIED;
        else {
            validInput = element;
            return null;
        }
    }

    public Element getValidInput() {
        return this.validInput;
    }

    public void initBoardVerifier() {
        copyOfBoard = boardUtil.getCopyOfBoard();
    }

    public void reinitializeBoardVerifier() {
        validInput = null;
    }


    public GameStatus checkRoundInput(GameRound round) {
        Element element = round.getInput();
        GameStatus status = null;
        int numEmptySpaces = boardUtil.getBoardStatistics()[BoardUtil.EMPTY_SPACE_INDEX];
        char winningChar = copyOfBoard[element.getRowIndex()][element.getColIndex()];
        boolean isThereAWin = isRowOrColumnThreeConsecutiveChars(element) || areDiagonalsThreeConsecutiveChars(element);

        if(isThereAWin == false) {
            if(numEmptySpaces > 0)
                status = GameStatus.GAME_NOT_FINISHED;
            else if(numEmptySpaces == 0)
                status = GameStatus.DRAW;
        } else
            status = winningChar == BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX] ? GameStatus.X_WINS : GameStatus.O_WINS;
        return status;
    }

    public boolean isRowOrColumnThreeConsecutiveChars(Element element) {
        int rowIndex = element.getRowIndex();
        int colIndex = element.getColIndex();
        char characterToCheck = copyOfBoard[rowIndex][colIndex];
        boolean rowCheck = true, colCheck = true;

        for(int i = 0; i < BoardUtil.BOARD_SIZE; i++) {
            if(rowCheck && copyOfBoard[rowIndex][i] != characterToCheck)
                rowCheck = false;
            if(colCheck && copyOfBoard[i][colIndex] != characterToCheck)
                colCheck = false;
            if(!(rowCheck || colCheck))
                break;
        }
        return rowCheck || colCheck;
    }

    public boolean areDiagonalsThreeConsecutiveChars(Element element) {
        int rowIndex = element.getRowIndex();
        int colIndex = element.getColIndex();
        char characterToCheck = copyOfBoard[rowIndex][colIndex];

        boolean mainDiagonal = rowIndex == colIndex,
                sideDiagonal = rowIndex == BoardUtil.BOARD_SIZE-1-colIndex;
        if(mainDiagonal || sideDiagonal) {
            for(int i = 0; i < BoardUtil.BOARD_SIZE; i++) {
                if(mainDiagonal && copyOfBoard[i][i] != characterToCheck)
                    mainDiagonal = false;
                if(sideDiagonal && copyOfBoard[i][BoardUtil.BOARD_SIZE-1-i] != characterToCheck)
                    sideDiagonal = false;
                if(!(mainDiagonal || sideDiagonal))
                    break;
            }
        }
        return mainDiagonal || sideDiagonal;
    }

    public void updateCopyBoard(Element element) {
        copyOfBoard[element.getRowIndex()][element.getColIndex()] = element.getValue();
    }
}

class BoardVerifierWithAILogic extends BoardVerifier {
    public BoardVerifierWithAILogic(BoardUtilWithAILogic boardUtil) {
        this.boardUtil = boardUtil;
    }

    public boolean shouldThisElementBeAvoided(Element element, char value) {
        copyOfBoard[element.getRowIndex()][element.getColIndex()] = value;
        boolean isThereAWin = isRowOrColumnThreeConsecutiveChars(element) || areDiagonalsThreeConsecutiveChars(element);
        copyOfBoard[element.getRowIndex()][element.getColIndex()] = BoardUtil.CHARS_ALLOWED[BoardUtil.EMPTY_SPACE_INDEX];
        return isThereAWin;
    }
}

class FillBoardAndTestBoardVerifier extends BoardVerifier {

    public FillBoardAndTestBoardVerifier(FillBoardAndTestBoardUtil boardUtil) {
        this.boardUtil = boardUtil;
    }

    public ElementError isBoardStringValid(String boardString) {
        if(boardString.length() != BoardUtil.BOARD_SIZE*BoardUtil.BOARD_SIZE)
            return ElementError.INVALID_BOARD_LENGTH;

        for(char ch : boardString.toCharArray()) {
            if(!String.valueOf(BoardUtil.CHARS_ALLOWED).contains(String.valueOf(ch)))
                return ElementError.BOARD_INVALID_CHAR;
        }

        int numOfXs = (int)boardString.chars()
                .filter(c->(char)c == BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX])
                .count();
        int numOfOs = (int)boardString.chars()
                .filter(c->(char)c == BoardUtil.CHARS_ALLOWED[BoardUtil.O_INDEX])
                .count();
        int difference = Math.abs(numOfXs-numOfOs);
        if(!(difference == 0 || difference == 1))
            return ElementError.BOARD_INVALID_STATE;
        return null;

    }

    public GameStatus checkFullBoard() {
        GameStatus status = null;
        int numEmptySpaces = boardUtil.getBoardStatistics()[BoardUtil.EMPTY_SPACE_INDEX];
        char winningChar = isThreeConsecutiveChars();

        if(winningChar == '\u0000') {
            if(numEmptySpaces > 0)
                status = GameStatus.GAME_NOT_FINISHED;
            else if(numEmptySpaces == 0)
                status = GameStatus.DRAW;
        } else
            status = winningChar == BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX] ? GameStatus.X_WINS : GameStatus.O_WINS;
        return status;
    }

    public char isThreeConsecutiveChars() {
        char rowChar = copyOfBoard[0][0], colChar = copyOfBoard[0][0];
        boolean checkRow = true, checkColumn = true;

        for(int i = 0; i<BoardUtil.BOARD_SIZE; i++) {
            rowChar = copyOfBoard[i][0];
            colChar = copyOfBoard[0][i];
            checkRow = rowChar != '_';
            checkColumn = colChar != '_';
            for(int j = 0; j<BoardUtil.BOARD_SIZE&&(checkColumn||checkRow); j++) {
                if(checkRow) {
                    if(copyOfBoard[i][j] != rowChar)
                        checkRow = false;
                }

                if(checkColumn) {
                    if(copyOfBoard[j][i] != colChar)
                        checkColumn = false;
                }
            }
            if(checkRow || checkColumn)
                break;
        }
        char element = checkRow || checkColumn ? (checkRow ? rowChar : colChar) : '\u0000';
        if(element == '\u0000')
            return isThreeConsecutiveCharsInDiagonals();
        return element;
    }

    public char isThreeConsecutiveCharsInDiagonals() {
        char mainDiagonalElement = copyOfBoard[0][0], sideDiagonalElement = copyOfBoard[0][BoardUtil.BOARD_SIZE-1];
        boolean checkMainDiagonal = mainDiagonalElement != '_', checkSideDiagonal = sideDiagonalElement != '_';
        for(int i = 1; i < BoardUtil.BOARD_SIZE; i++) {
            if(checkMainDiagonal) {
                if(copyOfBoard[i][i] != mainDiagonalElement)
                    checkMainDiagonal = false;
            }

            if(checkSideDiagonal) {
                if(copyOfBoard[i][BoardUtil.BOARD_SIZE-1-i] != sideDiagonalElement)
                    checkSideDiagonal = false;
            }
        }
        return checkMainDiagonal || checkSideDiagonal ? (checkMainDiagonal ? mainDiagonalElement : sideDiagonalElement) : '\u0000';
    }

}

class Element {
    private int rowIndex;
    private int colIndex;
    private char value;

    public Element(int rowIndex, int colIndex) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }

    public int getRowIndex() {
        return this.rowIndex;
    }

    public int getColIndex() {
        return this.colIndex;
    }

    public void setValue(char value) {
        this.value = value;
    }

    public char getValue() {
        return this.value;
    }

    public static Element getElement(int rowIndex, int colIndex) {
        int index1 = BoardUtil.BOARD_SIZE - colIndex;
        int index2 = rowIndex - 1;
        return new Element(index1, index2);
    }

    public static Element createElement(int rowIndex, int colIndex) {
        return new Element(rowIndex, colIndex);
    }

}

class GameRound {

    private BoardUtil boardUtil;
    private RoundType roundType;
    private Element inputElement;
    private ComputerStrategy strategy;


    public GameRound(BoardUtil boardUtil, RoundType roundType) {
        this.boardUtil = boardUtil;
        this.roundType = roundType;
    }

    public GameRound(BoardUtil boardUtil, RoundType roundType, ComputerStrategy strategy) {
        this(boardUtil, roundType);
        this.strategy = strategy;
    }

    public Element getNextMove() {
        if(roundType != RoundType.MACHINE_ROUND)
            getMoveFromUser();
        return inputElement;
    }

    public BoardUtil getBoardUtil() {
        return boardUtil;
    }

    public Element getNextMove(char value) {
        if(roundType != RoundType.MACHINE_ROUND)
            getMoveFromUser();
        else
            getMoveFromStrategy(value);
        return inputElement;
    }

    public void getMoveFromStrategy(char value) {
        inputElement = strategy.makeMove(this, value);
    }

    public void getMoveFromUser() {
        boolean isInputValid = false;
        Element element = null;
        while(!isInputValid) {
            System.out.print("Enter the coordinates: ");
            String inputLine = boardUtil.getGame().getScanner().nextLine();
            ElementError error = boardUtil.getBoardVerifier().isInputValid(inputLine);
            if(error != null) {
                System.out.println(error.getMessage());
                continue;
            } else {
                isInputValid = true;
                inputElement = boardUtil.getBoardVerifier().getValidInput();
            }
        }
    }

    public Element getInput() {
        return inputElement;
    }

    public void setInputElement(Element inputElement) {
        this.inputElement = inputElement;
    }

}
interface ComputerStrategy {
    public static final int RANDOM_SEED = 1000;
    public static final Random RANDOM = new Random(RANDOM_SEED);
    public Element makeMove(GameRound round, char value);
}
abstract class ConcreteComputerStrategy implements ComputerStrategy {
    protected char flipCharacter(char value) {
        char testWithChar = value == BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX] ?
                BoardUtil.CHARS_ALLOWED[BoardUtil.O_INDEX] :
                BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX];
        return testWithChar;

    }

    protected Element getRandomEmptySpace(List<Element> selectedSpaces) {
        int rand = RANDOM.nextInt(selectedSpaces.size());
        return selectedSpaces.get(rand);
    }
}

class EasyGameStrategy extends ConcreteComputerStrategy {

    public Element makeMove(GameRound round, char value) {
        System.out.println("Making move level \"easy\"");
        char testWithChar = flipCharacter(value);

        BoardUtilWithAILogic boardUtilWithAILogic = (BoardUtilWithAILogic)round.getBoardUtil();
        BoardVerifierWithAILogic boardVerifierWithAILogic = (BoardVerifierWithAILogic)boardUtilWithAILogic
                .getBoardVerifier();
        List<Element> emptySpaces = boardUtilWithAILogic.getEmptySpaces();
        List<Element> selectedSpaces = emptySpaces;

        selectedSpaces = emptySpaces.stream()
                    .filter(s -> !boardVerifierWithAILogic.shouldThisElementBeAvoided(s, testWithChar))
                    .collect(Collectors.toList());
        selectedSpaces = selectedSpaces.stream()
                    .filter(s -> !boardVerifierWithAILogic.shouldThisElementBeAvoided(s, value))
                    .collect(Collectors.toList());
        if(selectedSpaces.size() == 0)
            selectedSpaces = emptySpaces;
        return getRandomEmptySpace(selectedSpaces);

    }
}

class MediumGameStrategy extends ConcreteComputerStrategy {

    @Override
    public Element makeMove(GameRound round, char value) {
        System.out.println("Making move level \"medium\"");
        char testWithChar = flipCharacter(value);

        BoardUtilWithAILogic boardUtilWithAILogic = (BoardUtilWithAILogic)round.getBoardUtil();
        BoardVerifierWithAILogic boardVerifierWithAILogic = (BoardVerifierWithAILogic)boardUtilWithAILogic
                .getBoardVerifier();
        List<Element> emptySpaces = boardUtilWithAILogic.getEmptySpaces();
        List<Element> selectedSpaces = emptySpaces;

        selectedSpaces = emptySpaces.stream()
                    .filter(s -> boardVerifierWithAILogic.shouldThisElementBeAvoided(s, value))
                    .collect(Collectors.toList());
        if(selectedSpaces.size() > 0)
            return selectedSpaces.get(0);
        selectedSpaces = emptySpaces.stream()
                    .filter(s -> boardVerifierWithAILogic.shouldThisElementBeAvoided(s, testWithChar))
                    .collect(Collectors.toList());
        if(selectedSpaces.size() > 0)
            return selectedSpaces.get(0);
        selectedSpaces = emptySpaces;
        int rand = RANDOM.nextInt(selectedSpaces.size());
        return selectedSpaces.get(rand);
    }
}

class HardGameStrategy extends ConcreteComputerStrategy {

    private char myChar;

    @Override
    public Element makeMove(GameRound round, char value) {
        System.out.println("Making move level \"hard\"");
        this.myChar = value;
        char testWithChar = flipCharacter(value);

        BoardUtilWithAILogic boardUtilWithAILogic = (BoardUtilWithAILogic)round.getBoardUtil();
        BoardUtilWithAILogic newBoardUtilWithAILogic = new BoardUtilWithAILogic(boardUtilWithAILogic);
        Move move = miniMaxCall(newBoardUtilWithAILogic, null, 0, value);
        return move.getElement();

    }

    private Move miniMaxCall(BoardUtilWithAILogic boardUtilWithAILogic, GameRound round, int depth, char value) {
        depth+=1;
        if(round != null) {
            GameStatus status = boardUtilWithAILogic.getGameStatus(round);
            //System.out.println("Is there a win? "+status);
            if(status == GameStatus.X_WINS && myChar == BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX] ||
                    status == GameStatus.O_WINS && myChar == BoardUtil.CHARS_ALLOWED[BoardUtil.O_INDEX]) {
                return Move.getMove(round.getInput(), 10-depth);
            } else if(status == GameStatus.X_WINS && myChar == BoardUtil.CHARS_ALLOWED[BoardUtil.O_INDEX] ||
                    status == GameStatus.O_WINS && myChar == BoardUtil.CHARS_ALLOWED[BoardUtil.X_INDEX]) {
                return Move.getMove(round.getInput(), depth-10);
            } else if(boardUtilWithAILogic.getBoardStatistics()[BoardUtil.EMPTY_SPACE_INDEX] == 0) {
                return Move.getMove(round.getInput(), 0);
            }
        }
        List<Move> moves = new ArrayList<>();
        List<Element> emptySpaces = boardUtilWithAILogic.getEmptySpaces();
        BoardUtilWithAILogic newBoardUtilWithAILogic = new BoardUtilWithAILogic(boardUtilWithAILogic);
        for(Element element : emptySpaces) {
            Move move = null;
            round = new GameRound(boardUtilWithAILogic, RoundType.MACHINE_ROUND);
            round.setInputElement(element);
            round.getInput().setValue(value);
            newBoardUtilWithAILogic.updateBoard(element, RoundType.MACHINE_ROUND);
            Move result = miniMaxCall(newBoardUtilWithAILogic, round, depth, flipCharacter(value));
            move = Move.getMove(element, result.getScore());
            moves.add(move);
        }
        Move bestMove = null;
        if(value == myChar) {
            int bestScore = -100;

            for(Move move : moves) {
                if(move.getScore() > bestScore) {
                    bestScore = move.getScore();
                    bestMove = move;
                }
            }
        } else {
            int bestScore = 100;
            for(Move move : moves) {
                if(move.getScore() < bestScore) {
                    bestScore = move.getScore();
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }
}

class Move {
    private Element element;
    private int score;

    public Move(Element element, int score) {
        this.element = element;
        this.score = score;
    }

    public static Move getMove(Element element, int score) {
        return new Move(element, score);
    }

    public Element getElement() {
        return element;
    }

    public int getScore() {
        return score;
    }
}
/*
       System.out.println(moves.size());
        moves.stream().forEach(m -> {
            System.out.println("("+m.getElement().getRowIndex()+
                    ", "+m.getElement().getColIndex()+
                    ", "+m.getElement().getValue()+")");
            System.out.println(m.getScore());
        });

 */