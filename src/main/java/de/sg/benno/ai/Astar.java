/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021, stwe <https://github.com/stwe/Benno4j>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg.benno.ai;

import de.sg.benno.util.TileUtil;
import de.sg.benno.chunk.Ship4;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Stack;

import static de.sg.benno.content.World.WORLD_HEIGHT;
import static de.sg.benno.content.World.WORLD_WIDTH;
import static de.sg.benno.ogl.Log.LOGGER;
import static org.joml.Math.sqrt;

/**
 * Represents the A* algorithm.
 * A* search finds a path from start node to a goal node.
 */
public class Astar {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * There is no obstacle at this point.
     */
    public static final int PASSABLE = 0;

    /**
     * An obstacle.
     */
    public static final int OBSTACLE = 1;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Static class.
     */
    private Astar() {
    }

    //-------------------------------------------------
    // Find path
    //-------------------------------------------------

    /**
     * Finds the lowest cost path to a given target.
     *
     * @param start The start position in world space.
     * @param end The target position in world space.
     * @param obstacles The list with all the obstacles.
     *
     * @return A {@link ArrayList} with zero or multiple {@link Node} objects.
     */
    public static ArrayList<Node> findPathToTarget(Vector2i start, Vector2i end, ArrayList<Integer> obstacles) {
        return findPathToTarget(new Node(start), new Node(end), obstacles);
    }

    /**
     * Finds the lowest cost path to a given target.
     *
     * @param ship4 The start position of a {@link Ship4} in world space.
     * @param end The target position in world space.
     * @param obstacles The list with all the obstacles.
     *
     * @return A {@link ArrayList} with zero or multiple {@link Node} objects.
     */
    public static ArrayList<Node> findPathToTarget(Ship4 ship4, Vector2i end, ArrayList<Integer> obstacles) {
        return findPathToTarget(new Node(ship4.getPosition()), new Node(end), obstacles);
    }

    /**
     * Finds the lowest cost path to a given target.
     *
     * @param startNode The {@link Node} representing the start position in world space.
     * @param endNode The {@link Node} representing the target position in world space.
     * @param obstacles The list with all the obstacles.
     *
     * @return A {@link ArrayList} with zero or multiple {@link Node} objects.
     */
    public static ArrayList<Node> findPathToTarget(Node startNode, Node endNode, ArrayList<Integer> obstacles) {
        var empty = new ArrayList<Node>();

        if (!isValid(endNode, obstacles)) {
            LOGGER.debug("The end node is invalid.");
            return empty;
        }

        if (startNode.equals(endNode)) {
            LOGGER.debug("End node reached.");
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
                var f = Float.MAX_VALUE;
                var newNode = new Node();
                for (var openListNode : openList) {
                    if (openListNode.f < f) {
                        f = openListNode.f;
                        newNode = openListNode;
                    }
                }

                node = newNode;
                openList.remove(newNode);
            } while (!isValid(node, obstacles));

            xPos = node.position.x;
            yPos = node.position.y;
            index = TileUtil.getIndexFrom2D(xPos, yPos);
            closedList.set(index, true);

            for (var yOffset = -1; yOffset <= 1; yOffset++) {
                for (var xOffset = -1; xOffset <= 1; xOffset++) {
                    var newXPos = xPos + xOffset;
                    var newYPos = yPos + yOffset;
                    var newIndex = TileUtil.getIndexFrom2D(newXPos, newYPos);

                    if (isValid(newXPos, newYPos, obstacles)) {
                        if (isEndNodeReached(newXPos, newYPos, endNode)) {
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

        LOGGER.debug("No path found.");

        return empty;
    }

    //-------------------------------------------------
    // Make path
    //-------------------------------------------------

    /**
     * Putting the path inside a {@link ArrayList}.
     *
     * @param nodes The {@link Node} objects.
     * @param endNode The {@link Node} representing the target position in world space.
     *
     * @return {@link ArrayList<Node>}
     */
    private static ArrayList<Node> makePath(ArrayList<Node> nodes, Node endNode) {
        var xPos = endNode.position.x;
        var yPos = endNode.position.y;
        var index = TileUtil.getIndexFrom2D(xPos, yPos);

        var path = new Stack<Node>();
        var usablePath = new ArrayList<Node>();

        while (
                !(nodes.get(index).parentPosition.x == xPos && nodes.get(index).parentPosition.y == yPos) &&
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

    //-------------------------------------------------
    // Heuristic
    //-------------------------------------------------

    /**
     * Tells us how close we are to the goal.
     * @see <a href="https://theory.stanford.edu/~amitp/GameProgramming/Heuristics.html">Amit's Notes on A* Heuristics</a>
     *
     * @param x An x position in world space.
     * @param y A y position in world space.
     * @param endNode The {@link Node} representing the target position in world space.
     *
     * @return The distance to the goal.
     */
    private static float calculateHeuristic(int x, int y, Node endNode) {
        var xd = endNode.position.x - x;
        var yd = endNode.position.y - y;

        return sqrt(xd * xd + yd * yd);
    }

    //-------------------------------------------------
    // Validation
    //-------------------------------------------------

    /**
     * Check whether a position is valid - not an obstacle and not out of bounds.
     *
     * @param x The x position in world space.
     * @param y The y position in world space.
     * @param obstacles The list with all the obstacles.
     *
     * @return boolean
     */
    private static boolean isValid(int x, int y, ArrayList<Integer> obstacles) {
        if (x < 0 || y < 0 || x >= WORLD_WIDTH || y >= WORLD_HEIGHT) {
            return false;
        }

        return obstacles.get(TileUtil.getIndexFrom2D(x, y)) == PASSABLE;
    }

    /**
     * Check whether a position is valid - not an obstacle and not out of bounds.
     *
     * @param node The {@link Node} representing a position in world space.
     * @param obstacles The list with all the obstacles.
     *
     * @return boolean
     */
    private static boolean isValid(Node node, ArrayList<Integer> obstacles) {
        return isValid(node.position.x, node.position.y, obstacles);
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Checks whether the end node has been reached.
     *
     * @param x The x position in world space.
     * @param y The y position in world space.
     * @param endNode The {@link Node} representing the target position in world space.
     *
     * @return boolean
     */
    private static boolean isEndNodeReached(int x, int y, Node endNode) {
        return x == endNode.position.x && y == endNode.position.y;
    }
}
