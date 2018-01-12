package com.example.alina.tetris.figures;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;

/**
 * Created by Alina on 02.04.2017.
 */

public class LongFigure extends Figure {

    public LongFigure(int widthSquare) {
        super(widthSquare);
    }

    @Override
    public void initFigureMask() {
        super.initFigureMask();
        figureMask[0][0] = true;
        figureMask[1][0] = true;
        figureMask[2][0] = true;
        figureMask[3][0] = true;
    }

    @Override
    public int getWidthInSquare() {
        return 1;
    }

    @Override
    public int getHeightInSquare() {
        return 4;
    }

    @Override
    public Path getPath() {
        Path path = new Path();
        path.moveTo(point.x, point.y);
        path.lineTo(point.x + squareWidth, point.y);
        path.lineTo(point.x + squareWidth, point.y + squareWidth * 4);
        path.lineTo(point.x, point.y + squareWidth * 4);
        path.close();
        return path;
    }

    @Override
    public int getColor() {
        return Color.CYAN;
    }
}
