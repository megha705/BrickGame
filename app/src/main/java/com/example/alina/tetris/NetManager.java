package com.example.alina.tetris;


import android.util.Log;

import com.example.alina.tetris.figures.Figure;

import java.util.ArrayList;
import java.util.List;

public class NetManager {

    private Figure figure;

    private boolean[][] net;

    private boolean[][] zeroNet;

    private final List<Figure> figureListInNet = new ArrayList<>();

    public NetManager() {
        this.net = null;
    }

    public void initFigure(Figure figure) {
        figureListInNet.add(figure);
        this.figure = figureListInNet.get(figureListInNet.size() - 1);
        this.zeroNet = new boolean[figure.getHeightInSquare()][1];
        copyMaskToNet();
    }

    public void setNet(boolean[][] net) {
        this.net = net;
    }

    public void copyMaskToNet() {
        copyArrays(figure.figureMask.length, figure.figureMask, 0, net,
                figure.getStartX(), figure.figureMask[0].length);
    }

    private void moveFigure() {
        for (int i = 0; i < figure.figureMask.length; i++) {
            System.arraycopy(figure.figureMask[i], 0,
                    net[figure.coordinatesInPlayingArea.y + i],
                    figure.coordinatesInPlayingArea.x, figure.getWidthInSquare());
        }
    }

    private void resetNetAfterMoving(int destinationPosition) {
        for (int i = 0; i < zeroNet.length; i++) {
            System.arraycopy(zeroNet[i], 0,
                    net[figure.coordinatesInPlayingArea.y + i], destinationPosition, 1);
        }
    }

    private void setFalseNet(boolean[][] net) {
        for (int i = 0; i < net.length; i++) {
            for (int j = 0; j < net[0].length; j++) {
                net[i][j] = false;
            }
        }
    }

    public void moveLeftInNet() {
        setFalseNet(zeroNet);
        moveFigure();
        resetNetAfterMoving(figure.coordinatesInPlayingArea.x
                + figure.getWidthInSquare());
    }

    public void moveRightInNet() {
        setFalseNet(zeroNet);
        moveFigure();
        resetNetAfterMoving(figure.coordinatesInPlayingArea.x - 1);
    }

    public void moveDownInNet() {
        boolean[][]zeroNet = new boolean[1][figure.getWidthInSquare()];
        int coordinateY = figure.coordinatesInPlayingArea.y;
        coordinateY--;
        for (int i = 0; i < figure.figureMask.length; i++) {
            System.arraycopy(figure.figureMask[i], 0,
                    net[coordinateY + i + 1],
                    figure.coordinatesInPlayingArea.x, figure.figureMask[i].length);
            }
        for (int i = 0; i < zeroNet.length; i++) {
            System.arraycopy(zeroNet[i], 0,
                    net[figure.coordinatesInPlayingArea.y - 1],
                    figure.coordinatesInPlayingArea.x, figure.figureMask[i].length);
        }
    }

    private void copyArrays(int size, boolean[][] sourceArray, int sourcePosition,
                           boolean[][] destinationArray, int destinationPosition, int length) {
        for (int i = 0; i < size; i++) {
            System.arraycopy(sourceArray[i], sourcePosition, destinationArray[i],
                    destinationPosition, length);
        }
    }

    public void printNet() {
        if (net == null) {
            return;
        }
        String str = "";
        for (boolean[] aNet : net) {
            for (int j = 0; j < net[0].length; j++) {
                str += aNet[j] ? 1 : 0;
                str += " ";
            }
            str += '\n';
            Log.d("logNet", str);
        }
    }

    public void initNet(int horizontalSquareCount, int verticalSquareCount) {
        setNet(new boolean[horizontalSquareCount][verticalSquareCount]);
        setFalseNet(net);
    }
}
