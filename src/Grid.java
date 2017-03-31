import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Grid {
    
    final int TICKRATE = 30; // ticks/second
    final int GRIDSIZE = 30; // # boxes^2 in grid
    final int SQUARESIZE = 15; // SQUARESIZExSQUARESIZE pixels in each box
    int ticks = -1;
    boolean end;
    long startTime;
    long endTime;
    
    CopyOnWriteArrayList<Entity> positions; // list of entities
    
    public Grid() {
        
        // data setup
        positions = new CopyOnWriteArrayList<Entity>();
        
        // panel setup
        gridPanel panel = new gridPanel();
        panel.setPreferredSize(new Dimension((GRIDSIZE*SQUARESIZE)+1, (GRIDSIZE*SQUARESIZE)+1));
        panel.setBackground(Color.WHITE);
        
        // mouse setup (test)
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	if (e.getButton() == 1) { // left click for uninfected squares
            		for (int i = 0; i < 10; i++)
            			positions.add(new Entity(e.getX()/SQUARESIZE, e.getY()/SQUARESIZE, 0));
                	System.out.println("Current number of spawned entities: " + positions.size());
                } 
            	else if (e.getButton() == 3) { // right click for red "disease carrier"
            		positions.add(new Entity(e.getX()/SQUARESIZE, e.getY()/SQUARESIZE, 1));
                	System.out.println("Current number of spawned entities: " + positions.size());
                	startTime = System.currentTimeMillis();
                	if (ticks == -1) ticks = 0;
                }
            }
        });
        
        // frame setup
        JFrame frame = new JFrame("Grid");
        frame.setResizable(false);
        frame.setLayout(new FlowLayout());
        frame.add(panel);
        frame.setLocation(200, 200);

        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // timer setup
		Timer clock = new Timer();
		
		clock.scheduleAtFixedRate(new TimerTask() {
			public void checkEnd() {
				end = true;
				if (positions.size() == 0) end = false; // not sure if the "if/else" is even necessary
				else {
					for (Entity p : positions) {
						if (p.color().equals(Color.GREEN)) {
							end = false;
						}
					}
				}
			}

			public void run() {
				checkEnd();
				if (end) {
					endTime = System.currentTimeMillis();
					System.out.println("Entire Environment infected in " + ((endTime - startTime)/1000.0) + " seconds (" + ticks + " ticks).");
					clock.cancel();
				}
				else {
					if (ticks != -1) ticks++;
					for (Entity p : positions) {
						p.update();
					}
					panel.repaint();
				}
			}
		}, 0, 1000 / TICKRATE);
    }
    
    public class gridPanel extends JPanel { // drawing the grid and its contents
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // draw grid lines
            g.setColor(Color.GRAY);
            for (int i = 0; i < GRIDSIZE+1; i++) {
                g.drawLine(0, i*SQUARESIZE, GRIDSIZE*SQUARESIZE, i*SQUARESIZE);
            }
            for (int i = 0; i < GRIDSIZE+1; i++) {
                g.drawLine(i*SQUARESIZE, 0, i*SQUARESIZE, GRIDSIZE*SQUARESIZE);
            }
            
            // draw entities
            for (Entity p : positions) {
            	g.setColor(p.color());
                g.fillRect((p.x * SQUARESIZE)+1, (p.y * SQUARESIZE)+1, SQUARESIZE-1, SQUARESIZE-1);
            }
        }
    }
    
    public class Entity { 
    	
        int x, y, nature;
        Entity closestEnt;
        boolean infected;
        
        public Entity(int x, int y, int nature) {
            this.x = x;
            this.y = y;
            this.nature = nature;
            if (nature == 0) infected = false;
            else infected = true;
        }
        
        public void update() { // what the entity does each tick depending on its nature
			if (infected) {
				ArrayList<Entity> nearby = new ArrayList<Entity>();
				for (Entity e : positions) {
					if (e.equals(this)) {
						// do nothing
					} else if (distanceFrom(e) <= 1) {
						nearby.add(e);
					}
				}
				for (Entity e : nearby) {
					e.infected = true;
				}
			}
			moveFreely();
        }
        
        private void moveFreely() {
    		int rand = (int)(Math.random()*4);
        	if (rand == 0) up();
        	else if (rand == 1) down();
        	else if (rand == 2) left();
        	else if (rand == 3) right();
        }
        
        public Color color() {
        	if (nature == 1) return Color.RED;
        	else if (infected) return Color.BLUE;
        	else return Color.GREEN; 
        }
        private double distanceFrom(Entity e) { // distance from other entities
        	return Math.sqrt(((e.x-x)*(e.x-x)) + ((e.y-y)*(e.y-y)));
        }

		private Entity findClosestEntity() {
			Entity closest = positions.get(0);
			for (int i = 0; i < positions.size(); i++) {
				if (positions.get(i).equals(this)) {
					// do nothing
				} else if (distanceFrom(positions.get(i)) < distanceFrom(closest)) {
					closest = positions.get(i);
				}
			}
			return closest;
		}
        
        private void up() {
            if(y-1 < 0) y = GRIDSIZE;
            else y-=1;
        }
        private void down() {
            if(y+1 > GRIDSIZE) y = 0;
            else y+=1;
        }
        private void left() {
            if(x-1 < 0) x = GRIDSIZE;
            else x-=1;
        }
        private void right() {
            if(x+1 > GRIDSIZE) x = 0;
            else x+=1;
        }
    }

    public static void main(String args[]) {
        new Grid();
    }
}