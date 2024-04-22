
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

// Obstacle class
class Obstacle {
    public int x,y,spriteIndex;

    public Obstacle(int x, int y, int spriteIndex) {
        this.x = x;
        this.y = y;
        this.spriteIndex = spriteIndex;
    }
}

// SpaceGame class
public class SpaceGame extends JFrame implements KeyListener {
    private final List<Obstacle> obstacles;
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 80;
    private static final int PLAYER_HEIGHT = 80;
    private static final int OBSTACLE_WIDTH = 10;
    private static final int OBSTACLE_HEIGHT = 10;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 15;
    private static final int PROJECTILE_SPEED = 12;
    private static double obstacleSpeed = 3;
    private static double obstacleGenerationRate = 0.04;
    private int level = 1;
    private int score = 0;
    private int health = 100;
    private BufferedImage shipImage;
    private BufferedImage shipExplosion;
    private BufferedImage spriteSheet;
    private final int spriteWidth = 64;
    private final int spriteHeight = 64;
    private final JPanel gamePanel;
    private final JLabel scoreLabel;
    private final JLabel healthLabel;
    private final JLabel timerLabel;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private List<Point> stars;
    private final Timer timer;
    private final Timer gameTimer;
    private int countdownTime = 60;
    ;
    private boolean isShieldActive;
    private boolean healthBoost;

    public SpaceGame() {
        // Setting up JFrame
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        playSound("open.wav");

        // Setting up JPanel
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        // Setting up score label
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setBounds(10, 10, 100, 20);
        gamePanel.add(scoreLabel);
        scoreLabel.setForeground(Color.BLUE);

        // Setting up health label
        healthLabel = new JLabel("Health: 100");
        healthLabel.setFont(new Font("Arial", Font.BOLD, 16));
        healthLabel.setBounds(10, 30, 100, 20);
        gamePanel.add(healthLabel);
        healthLabel.setForeground(Color.GREEN);

        // Setting up timer label
        timerLabel = new JLabel("Time: 60");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setBounds(10, 50, 100, 20);
        gamePanel.add(timerLabel);
        timerLabel.setForeground(Color.RED);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        // Setting up initial player position and state
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new java.util.ArrayList<>();

        // Loading images
        try {
            shipImage = ImageIO.read(new File("SpaceShip.png"));
            spriteSheet = ImageIO.read(new File("astro.png"));
            shipExplosion = ImageIO.read(new File("explosion.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Generate stars
        stars = generateStars(200);
        isShieldActive = false;
        healthBoost = false;

        // Timer for game loop
        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();

        //Timer for countdown
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdownTime--;
                timerLabel.setText("Time: " + countdownTime);
                if (countdownTime == 0) {
                    isGameOver = true;
                    update();
                    gamePanel.repaint();
                    gameTimer.stop();
                }
            }
        });
        gameTimer.start();

    }

    //Temporary message method
    private void temporaryMessage(String message) {

        Timer tempMessageTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                healthBoost = false;
            }
        });
        tempMessageTimer.setRepeats(false);
        tempMessageTimer.start();
    }
    //Activate shield method
    private void activateShield() {
        isShieldActive = true;
        Timer shieldTimer = new Timer(6000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isShieldActive = false;
            }
        });
        shieldTimer.setRepeats(false);
        shieldTimer.start();
    }
    //Restart game method
    private void restartGame() {
        score = 0;
        health = 100;
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        countdownTime = 30;
        gameTimer.start();
        obstacles.clear();
        playSound("open.wav");
        level = 1;
        obstacleSpeed = 3;
        obstacleGenerationRate = 0.04;
    }
    //Play sound method
    private void playSound(String soundName) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error with playing sound.");
            e.printStackTrace();
        }
    }
    //Generate random color
    private Color getRandomColor() {
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        return new Color(r, g, b);
    }
    //Generate stars
    private List<Point> generateStars(int numStars) {
        List<Point> stars = new java.util.ArrayList<>();
        for (int i = 0; i < numStars; i++) {
            int x = (int) (Math.random() * WIDTH);
            int y = (int) (Math.random() * HEIGHT);
            stars.add(new Point(x, y));
        }
        return stars;
    }

    //Draw method
    private void draw(Graphics g) {
        // Draw background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        //Draw stars
        for (Point star : stars) {
            g.setColor(getRandomColor());
            g.fillRect(star.x, star.y, 2, 2);
        }
        // Draw player
        g.drawImage(shipImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        if (health <= 0) {
            g.drawImage(shipExplosion, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        }
        // Draw projectile
        if (isProjectileVisible) {
            g.setColor(Color.ORANGE);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }
        // Draw obstacles
        for (Obstacle obstacle : obstacles) {
            if (spriteSheet != null) {
                // Calculate spriteX and spriteY
                int spriteX = obstacle.spriteIndex * spriteWidth; // Each sprite is 64 pixels wide
                int spriteY = 0; // Assuming all sprites are in the same row
                // Draw sprite
                g.drawImage(spriteSheet.getSubimage(spriteX, spriteY, spriteWidth, spriteHeight), obstacle.x, obstacle.y, null);
            }
        }
        //Draw Shield on SpaceShip
        if(isShieldActive) {
            //set transparency
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.fillOval(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            g.drawOval(playerX - 10, playerY - 10, PLAYER_WIDTH + 20, PLAYER_HEIGHT + 20);
        }

        // Draw game over screen
        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2 - 20);
            g.drawString("Score: " + score, WIDTH / 2 - 60, HEIGHT / 2 );
            g.drawString("Press Enter to Restart", WIDTH / 2 - 120, HEIGHT / 2 + 20);
            g.drawString("Press ESC to Exit", WIDTH / 2 - 100, HEIGHT / 2 + 40);
        }
        //Draw level
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Level: " + level, WIDTH - 70, 40);

        //Draw Health Boost
        if (healthBoost) {
            //left side
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Health Boost!", 20, 20);
        }
    }
    // Update method
    private void update() {
        if (!isGameOver) {
            // Move obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += obstacleSpeed;
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }
            // Generate new obstacles
            if (Math.random() < obstacleGenerationRate) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                int spriteIndex = (int) (Math.random() * 4);
                obstacles.add(new Obstacle(obstacleX, 0, spriteIndex));
            }
            //Generate new stars
            if (Math.random() < 0.15) {
                stars = generateStars(200);
            }
            // Move projectile
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }
            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (Obstacle obstacle : obstacles) {
                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect) && !isShieldActive) {
                    playSound("gotHit.wav");
                    if (obstacle.spriteIndex == 0) {
                        health -= 20;
                    } else if (obstacle.spriteIndex == 1) {
                        health -= 15;
                    } else if (obstacle.spriteIndex == 2) {
                        health -= 10;
                    } else if (obstacle.spriteIndex == 3) {
                        health -= 5;
                    }
                    if (health <= 0) {
                        health = 0;
                        obstacles.remove(obstacle);
                        isGameOver = true;
                        playSound("explosion.wav");
                        break;
                    } else {
                        obstacles.remove(obstacle);
                        break;
                    }
                }
            }

            // Check collision with obstacle and projectile and update score based on obstacle type
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                // Use spriteWidth and spriteHeight for the obstacle rectangle
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, spriteWidth, spriteHeight);
                if (projectileRect.intersects(obstacleRect) && isProjectileVisible) {
                    playSound("hitAstro.wav");
                    if (obstacles.get(i).spriteIndex == 0) {
                        score += 20;
                    } else if (obstacles.get(i).spriteIndex == 1) {
                        score += 15;
                    } else if (obstacles.get(i).spriteIndex == 2) {
                        score += 10;
                    } else if (obstacles.get(i).spriteIndex == 3) {
                        score += 5;
                    }
                    obstacles.remove(i);
                    isProjectileVisible = false;
                    break;
                }
            }
            // Update labels
            scoreLabel.setText("Score: " + score);
            healthLabel.setText("Health: " + health);

            //Random Health Boost if health under 50
            if (Math.random() < 0.005 && health < 50) {
                health += 10;
                healthBoost = true;
                temporaryMessage("Health Boost! +10");
            }
            //Increase level
            if (score >= 100 && level == 1) {
                level = 2;
                obstacleSpeed = 4;
                obstacleGenerationRate += 0.005;
            } else if (score >= 200 && level == 2) {
                level = 3;
                obstacleSpeed = 4.5;
                obstacleGenerationRate += 0.005;
            } else if (score >= 300 && level == 3) {
                level = 4;
                obstacleSpeed = 5;
                obstacleGenerationRate += 0.005;
            } else if (score >= 400 && level == 4) {
                level = 5;
                obstacleSpeed = 5.5;
                obstacleGenerationRate += 0.005;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode(); // Get the key code
        // Check if the key code is the escape key
        if (keyCode == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
        // Check if the game is over and the key code is Z
        else if (!isGameOver && keyCode == KeyEvent.VK_Z){
            activateShield();
        }
        // Check if the game is over and the key code is Enter
        else if (isGameOver && keyCode == KeyEvent.VK_ENTER) {
            restartGame();
        }
        // Check if the game is not over
        else if (!isGameOver) {
            // Check if the key code is the left arrow key and the player is not at the left edge
            if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
                playerX -= PLAYER_SPEED;
            }
            // Check if the key code is the right arrow key and the player in not at the right edge
            else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
                playerX += PLAYER_SPEED;
            }
            // Check if the key code is the space key and the player is not firing
            else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
                playSound("laser.wav");
                isFiring = true;
                projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
                projectileY = playerY;
                isProjectileVisible = true;
                // Create a new thread to limit the firing rate
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500); // Limit firing rate
                            isFiring = false;
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();// Start the thread
            }
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}
