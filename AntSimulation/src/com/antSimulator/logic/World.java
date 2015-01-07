package com.antSimulator.logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class World {

	public static final int NUM_OF_ANTS = 80;
	public static final int WIDTH = 100;
	public static final int HEIGHT = 100;
	public static final int FOOD_WIDTH = 5;
	public static final int FOOD_HEIGHT = 5;
	private Cell[][] matrix;
	private boolean[][] lockedCell;
	private BlockingQueue<Ant> ants;
	private Point nest;
	private int nestlevel;
	private ArrayList<Point> food;

	public World() {

		loadWorld();
		spawnAnts();

	}

	private void spawnAnts() {
		ants = new ArrayBlockingQueue<Ant>(NUM_OF_ANTS + 1);

		for (int i = 0; i < NUM_OF_ANTS; i++)
			try {

				Ant a = new Ant(nestlevel, (Point) nest.clone(), i + 1);
				ants.put(a);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

	}

	private void loadWorld() {

		matrix = new Cell[WIDTH][HEIGHT];
		lockedCell = new boolean[WIDTH][HEIGHT];
		initWorld();

		nest = new Point(50, 55);
		food = new ArrayList<Point>();
		food.add(new Point(10, 20));
		food.add(new Point(30, 10));

		nestlevel = getWorld()[5][5].getG().getLevel();

	}

	private void initWorld() {
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				matrix[i][j] = new Cell(i, j,
						new Random().nextInt(GroundState.MAXLEVEL));
				lockedCell[i][j] = false;
			}
		}

		for (int k = 0; k < 5; k++) {

			int i = new Random().nextInt(HEIGHT);
			int j = new Random().nextInt(WIDTH);
			matrix[i][j] = new Cell(i, j, GroundState.MAXLEVEL);
		}

	}

	public Cell[][] getWorld() {
		return matrix;
	}

	public void setWorld(Cell[][] world) {
		this.matrix = world;
	}

	public Cell getAvailableCell(int xPos, int yPos) {

		if (xPos < 0 || xPos >= WIDTH || yPos < 0 || yPos >= HEIGHT)
			return null;
		else if (matrix[xPos][yPos].getG().getLevel() == GroundState.MAXLEVEL)
			return null;
		else if (matrix[xPos][yPos].getA() != null)
			return null;
		else
			return matrix[xPos][yPos];
	}

	public Cell getCell(int xPos, int yPos) {

		if (xPos < 0 || xPos >= WIDTH || yPos < 0 || yPos >= HEIGHT)
			return null;
		else
			return matrix[xPos][yPos];
	}

	public boolean[][] getLockedCell() {
		return lockedCell;
	}

	public void setLockedCell(boolean[][] lockedCell) {
		this.lockedCell = lockedCell;
	}

	public void unlockCell(int x, int y, int nx, int ny) {
		Manager.getInstance().lock.lock();
		try {

			lockedCell[x][y] = false;
			lockedCell[nx][ny] = false;

			Manager.getInstance().condition.signalAll();

		} finally {
			Manager.getInstance().lock.unlock();
		}
	}

	public void lockCell(int x, int y, int nx, int ny) {

		Manager.getInstance().lock.lock();
		try {

			while (lockedCell[x][y] && lockedCell[nx][ny])
				try {

					Manager.getInstance().condition.signalAll();
					Manager.getInstance().condition.await();

				} catch (InterruptedException e) {

					e.printStackTrace();
				}

			lockedCell[x][y] = true;
			lockedCell[nx][ny] = true;

		} finally {
			Manager.getInstance().lock.unlock();
		}

	}

	public BlockingQueue<Ant> getAnts() {
		return ants;
	}

	public void setAnts(BlockingQueue<Ant> ants) {
		this.ants = ants;
	}

	public void setCell(Cell cell) {
		this.matrix[cell.getX()][cell.getY()] = cell;
	}

	public ArrayList<Point> getFood() {
		return food;
	}

	public void setFood(ArrayList<Point> food) {
		this.food = food;
	}

	public Point getNest() {
		return nest;
	}

	public void setNest(Point nest) {
		this.nest = nest;
	}

}
