/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Stack;

import static de.sg.benno.World.WORLD_HEIGHT;
import static de.sg.benno.World.WORLD_WIDTH;

public class Astar {

    public ArrayList<Node> findPath(Node startNode, Node endNode) {
        var empty = new ArrayList<Node>();

        if (!isValid(endNode)) {
            return empty;
        }

        if (startNode.equals(endNode)) {
            return empty;
        }

        var closedList = new ArrayList<Boolean>();
        var allList = new ArrayList<Node>();

        for(int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                var node = new Node();
                node.position = new Vector2i(x, y);
                node.parentPosition = new Vector2i(-1, -1);
                node.g = Float.MAX_VALUE;
                node.h = Float.MAX_VALUE;
                node.f = Float.MAX_VALUE;

                allList.add(node);
                closedList.add(false);
            }
        }

        var xPos = startNode.position.x;
        var yPos = startNode.position.y;
        var index = TileUtil.getIndexFrom2D(xPos, yPos);

        allList.get(index).parentPosition.x = xPos;
        allList.get(index).parentPosition.y = yPos;
        allList.get(index).g = 0.0f;
        allList.get(index).h = 0.0f;
        allList.get(index).f = 0.0f;

        var openList = new ArrayList<Node>();
        openList.add(allList.get(index));

        while (!openList.isEmpty() && openList.size() < WORLD_WIDTH * WORLD_HEIGHT) {
            var node = new Node();

            do {
                var tmp = Float.MAX_VALUE;
                var itNode = new Node();
                for (var n : openList) {
                    var openListNode = n;

                    if (openListNode.f < tmp) {
                        tmp = openListNode.f;
                        itNode = n;
                    }
                }

                node = itNode;
                openList.remove(itNode);

            } while (!isValid(node));

            xPos = node.position.x;
            yPos = node.position.y;
            index = TileUtil.getIndexFrom2D(xPos, yPos);

            closedList.set(index, true);

            for (var yOffset = -1; yOffset <= 1; yOffset++) {
                for (var xOffset = -1; xOffset <= 1; xOffset++) {
                    var newXPos = xPos + xOffset;
                    var newYPos = yPos + yOffset;
                    var newIndex = TileUtil.getIndexFrom2D(newXPos, newYPos);

                    if (isValid(newXPos, newYPos)) {
                        if (newXPos == endNode.position.x && newYPos == endNode.position.y) {
                            allList.get(newIndex).parentPosition.x = xPos;
                            allList.get(newIndex).parentPosition.y = yPos;

                            return makePath(allList, endNode);
                        }

                        if (!closedList.get(newIndex)) {
                            var newG = node.g + 1.0f;
                            var newH = calculateHeuristic(newXPos, newYPos, endNode);
                            var newF = newG + newH;

                            if (allList.get(newIndex).f == Float.MAX_VALUE || allList.get(newIndex).f > newF) {
                                allList.get(newIndex).parentPosition.x = xPos;
                                allList.get(newIndex).parentPosition.y = yPos;
                                allList.get(newIndex).g = newG;
                                allList.get(newIndex).h = newH;
                                allList.get(newIndex).f = newF;

                                openList.add(allList.get(newIndex));
                            }
                        }
                    }
                }
            }
        }

        return empty;
    }

    public ArrayList<Node> findPathToMapPosition(
            Vector2i startPosition,
            Vector2i targetPosition
    ) {
        var startNode = new Node();
        var endNode = new Node();

        startNode.position = startPosition;
        endNode.position = targetPosition;

        return findPath(startNode, endNode);
    }

    private ArrayList<Node> makePath(ArrayList<Node> nodes, Node endNode) {
        var xPos = endNode.position.x;
        var yPos = endNode.position.y;
        var index = TileUtil.getIndexFrom2D(xPos, yPos);

        var path = new Stack<Node>();
        var usablePath = new ArrayList<Node>();

        while (
                !(nodes.get(index).parentPosition.x == xPos &&
                        nodes.get(index).parentPosition.y == yPos) &&
                        nodes.get(index).position.x != -1 && nodes.get(index).position.y != -1
        ) {
            path.push(nodes.get(index));
            var tmpX = nodes.get(index).parentPosition.x;
            var tmpY = nodes.get(index).parentPosition.y;
            xPos = tmpX;
            yPos = tmpY;
            index = TileUtil.getIndexFrom2D(xPos, yPos);
        }

        path.push(nodes.get(index));

        while (!path.empty()) {
            var top = path.peek();
            path.pop();
            usablePath.add(top);
        }

        return usablePath;
    }

    private float calculateHeuristic(int x, int y, Node endNode) {
        var xd = endNode.position.x - x;
        var yd = endNode.position.y - y;

        return (float)Math.sqrt(xd * xd + yd * yd);
    }

    private boolean isValid(int x, int y) {
        if (x < 0 || y < 0 || x >= WORLD_WIDTH || y >= WORLD_HEIGHT) {
            return false;
        }

        // todo search map obstacles

        return true;
    }

    private boolean isValid(Node node) {
        return isValid(node.position.x, node.position.y);
    }
}
