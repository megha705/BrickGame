package com.example.alina.tetris.ui.main.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.example.alina.tetris.ui.main.NetManager;
import com.example.alina.tetris.R;
import com.example.alina.tetris.Values;
import com.example.alina.tetris.ui.score.ScoreCounter;
import com.example.alina.tetris.enums.FigureState;
import com.example.alina.tetris.enums.FigureType;
import com.example.alina.tetris.figures.Figure;
import com.example.alina.tetris.figures.factory.FigureCreator;
import com.example.alina.tetris.figures.factory.FigureFactory;
import com.example.alina.tetris.listeners.OnNetChangedListener;
import com.example.alina.tetris.listeners.OnTimerStateChangedListener;
import com.example.alina.tetris.utils.CustomArrayList;

import static com.example.alina.tetris.Values.COUNT_DOWN_INTERVAL;
import static com.example.alina.tetris.Values.GAME_OVER_DELAY_IN_MILLIS;
import static com.example.alina.tetris.Values.GAME_OVER_TEXT;
import static com.example.alina.tetris.Values.LINE_WIDTH;
import static com.example.alina.tetris.Values.MILLIS_IN_FUTURE;
import static com.example.alina.tetris.Values.SQUARE_COUNT_HORIZONTAL;

/**
 * Created by Alina on 18.03.2017.
 */

public class PlayingAreaView extends View implements OnNetChangedListener {

    private int widthOfSquareSide;
    private int verticalSquareCount;
    private int screenHeight;
    private int screenWidth;
    private int scale;
    private boolean isTimerRunning;

    //todo remove this, there is no need to keep lists
    private final CustomArrayList<FigureType> figureTypeList = new CustomArrayList<>();
    private final CustomArrayList<Figure> figureList = new CustomArrayList<>();

    private NetManager netManager;
    private FigureCreator figureCreator;
    private ScoreView scoreView;
    private PreviewAreaView previewAreaView;
    private ScoreCounter scoreCounter;

    private Paint paint;

    private CountDownTimer timer;

    private Context context;
    private OnTimerStateChangedListener onTimerStateChangedListener;

    //todo remove this
    public static int FIGURE_TYPE_LIST_SIZE = 0;

    public PlayingAreaView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PlayingAreaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayingAreaView(@NonNull Context context, @Nullable AttributeSet attrs,
                           @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        figureCreator = new FigureCreator();
        scoreCounter = new ScoreCounter(getContext());
        this.context = context;
        this.isTimerRunning = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(LINE_WIDTH);
        drawVerticalLines(canvas);
        drawHorizontalLines(canvas);
        for (Figure figure : figureList) {
            if (figure != null && figure.getState() == FigureState.MOVING && isTimerRunning)
                startMoveDown();
        }
        if (netManager != null && netManager.getStoppedFiguresPaths() != null) {
            for (Path squarePath : netManager.getStoppedFiguresPaths()) {
                paint.setColor(getResources().getColor(R.color.zFigure));
                canvas.drawPath(squarePath, paint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        widthOfSquareSide = MeasureSpec.getSize(widthMeasureSpec) / SQUARE_COUNT_HORIZONTAL;
        verticalSquareCount = MeasureSpec.getSize(heightMeasureSpec) / widthOfSquareSide;
        scale = widthOfSquareSide - (MeasureSpec.getSize(heightMeasureSpec) % widthOfSquareSide);
        screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
    }

    public void cleanup() {
        scoreCounter.putNewScore(scoreView.getScore());
        cancelTimer();
        scoreView.setStartValue();
        netManager = null;
        figureList.clear();
        figureTypeList.clear();
    }

    public void setScoreView(ScoreView scoreView) {
        this.scoreView = scoreView;
    }

    public void setPreviewAreaView(PreviewAreaView previewAreaView) {
        this.previewAreaView = previewAreaView;
    }

    private void drawHorizontalLines(Canvas canvas) {
        for (int i = 1; i <= verticalSquareCount; i++) {
            canvas.drawLine(0, screenHeight - widthOfSquareSide * i, screenWidth,
                    screenHeight - widthOfSquareSide * i, paint);
        }
    }

    private void drawVerticalLines(Canvas canvas) {
        for (int i = 1; i <= SQUARE_COUNT_HORIZONTAL; i++) {
            canvas.drawLine(i * widthOfSquareSide, 0, i * widthOfSquareSide,
                    screenHeight, paint);
        }
    }

    public void setOnTimerStateChanged(OnTimerStateChangedListener onTimerStateChangedListener) {
        this.onTimerStateChangedListener = onTimerStateChangedListener;
    }

    public boolean getTimerState() {
        return isTimerRunning;
    }

    public void handleTimerState() {
        if (isTimerRunning) {
            cancelTimer();
        } else {
            startTimer();
        }
        isTimerRunning = !isTimerRunning;
        onTimerStateChangedListener.isTimerRunning(isTimerRunning);
    }

    public void startTimer() {
        if (timer != null) {
            timer.start();
        }
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void startMoveDown() {
        netManager.printNet();
        cancelTimer();
        timer = new CountDownTimer(MILLIS_IN_FUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                isTimerRunning = true;
            }

            public void onFinish() {
                if (figureList.getLast().getState() == FigureState.MOVING) {
                    figureList.getLast().moveDown();
                    netManager.moveDownInNet();
                    invalidate();
                }
            }
        };
        if (!netManager.isNetFreeToMoveDown()) {
            netManager.changeFigureState();
            cancelTimer();
        } else {
            startTimer();
        }
    }

    public void fastMoveDown() {
        if (figureList.size() > 0 && isTimerRunning) {
            cancelTimer();
            while (figureList.getLast().getState() == FigureState.MOVING) {
                if (!netManager.isNetFreeToMoveDown()) {
                    netManager.changeFigureState();
                    break;
                }
                figureList.getLast().moveDown();
                netManager.moveDownInNet();
            }
            invalidate();
        }
    }

    public void moveRightFast() {
        if (figureList.size() > 0 && isTimerRunning) {
            cancelTimer();
            while (netManager.isNetFreeToMoveRight()) {
                netManager.resetMaskBeforeMoveWithFalse();
                figureList.getLast().moveRight();
                netManager.moveRightInNet();
            }
            invalidate();
        }
    }

    public void moveLeftFast() {
        if (figureList.size() > 0 && isTimerRunning) {
            cancelTimer();
            while (netManager.isNetFreeToMoveLeft()) {
                netManager.resetMaskBeforeMoveWithFalse();
                figureList.getLast().moveLeft();
                netManager.moveLeftInNet();
            }
            invalidate();
        }
    }

    //todo think about the restrictions
    public void rotate() {
        if (figureList.size() > 0 && figureList.getLast().getState() == FigureState.MOVING
                && figureList.getLast().getRotatedFigure() != null && isTimerRunning) {
            Figure figure = FigureFactory.getFigure(figureList.getLast().getRotatedFigure(),
                    widthOfSquareSide, scale, context, figureList.getLast().point);
            if (figure != null) {
                figure.initFigureMask();
            }
            if (netManager.canRotate(figure)) {
                figureList.set(figureList.size() - 1, figure);
                netManager.checkBottomLine();
                netManager.initRotatedFigure(figure);
            }
        }
    }

    private void createFigure() {
        figureTypeList.add(figureCreator.getCurrentFigureType());
        FIGURE_TYPE_LIST_SIZE = figureTypeList.size();
        Figure figure = FigureFactory.getFigure(figureTypeList.getLast(), widthOfSquareSide,
                scale, context);
        figureList.add(figure);
        if (figure != null) {
            figure.initFigureMask();
            if (netManager == null) {
                netManager = new NetManager();
                netManager.initNet(verticalSquareCount, SQUARE_COUNT_HORIZONTAL, widthOfSquareSide, scale);
            }
            if (!netManager.isVerticalLineTrue()) {
                netManager.setOnNetChangedListener(this);
                netManager.checkBottomLine();
                netManager.initFigure(figure);
                netManager.printNet();
                invalidate();
            }
        }
    }

    public void moveLeft() {
        if (netManager != null && netManager.isNetFreeToMoveLeft() && isTimerRunning) {
            netManager.resetMaskBeforeMoveWithFalse();
            figureList.getLast().moveLeft();
            netManager.moveLeftInNet();
            netManager.printNet();
            invalidate();
        }
    }

    public void moveRight() {
        if (netManager != null && netManager.isNetFreeToMoveRight() && isTimerRunning) {
            netManager.resetMaskBeforeMoveWithFalse();
            figureList.getLast().moveRight();
            netManager.moveRightInNet();
            netManager.printNet();
            invalidate();
        }
    }

    public void createFigureWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                previewAreaView.drawNextFigure(FigureFactory.getFigure(figureCreator.getNextFigureType(),
                        widthOfSquareSide, context));
                createFigure();
            }
        }, Values.DELAY_IN_MILLIS);
    }

    @Override
    public void onFigureStoppedMove() {
        if (!netManager.isVerticalLineTrue()) {
            scoreView.sumScoreWhenFigureStopped();
            previewAreaView.drawNextFigure(FigureFactory.getFigure(figureCreator.createNextFigure(),
                    widthOfSquareSide, context));
            createFigure();
        }
    }

    @Override
    public void onBottomLineIsTrue() {
        scoreView.sumScoreWhenBottomLineIsTrue();
    }

    @Override
    public void onTopLineHasTrue() {
        Toast.makeText(context, GAME_OVER_TEXT, Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((Activity) context).finish();
            }
        }, GAME_OVER_DELAY_IN_MILLIS);
    }
}