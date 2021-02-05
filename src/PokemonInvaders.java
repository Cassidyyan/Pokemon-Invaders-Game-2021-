import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

//Cassidy
//January 2021,
/* Created variation of the game space invaders but with pokemon. A trainer shoots pokeballs to capture the pokemons falling from the sky.
 * The trainer will only have 3 lives per game
 * The trainer loses a life when it gets hit with a bubble or a lightning bolt
 * Every time the trainer clears all the aliens, they will re-spawn with an extra row 
 * Once the trainer loses all their lives or the aliens reach the end, a defeat sign would be displayed
 * There is no winning in the game, this game is based on how long you can survive for 
 * Scores are counted at the end and a high score will be displayed on the main screen*/

public class PokemonInvaders extends JPanel implements ActionListener, MouseListener, KeyListener, Runnable{
	// constructors
	static JFrame frame;
	Thread thread;
	
	/* Intro Audio Clips */
	Clip nationalPark, losingSong, snowbelleTheme, cynthiaTheme;
	Clip [] playlist = new Clip[4];
	// song tittle images
	Image [] playlistSongs = new Image[4];

	
	// music vars
	boolean music = false;
	int currentSong = 0;

	// players vars
	int lives = 3;
	boolean gameOver = true;
	int score = 0;
	int highscore = 0;
	boolean displayed = true;
	
	// different screens
	boolean menuScreen = false;
	boolean instructionScreen = false;
	boolean settingScreen = false;
	boolean losingScreen = false;
	boolean onScreen = true;
	
	/* GAME VARS */
	
	// set up vars
	final int fps = 60;
	final int length = 800;
	final int width = 1080;
	
	// mouse vars for menu
	int x = 0;
	int y = 0;
	
	// characters vars
	final int characterSize = 70;
	int xpos = length/2 - 40;
	int ypos = 735;
	int trainerOfChoice = 0;
	
	
	// alien vars
	int dropSpeed = 1;
	int [] alienXpos = new int[16];
	int [] alienYpos = new int [12];
	int [] alienColumnCount = new int [15];
	int rowsOfPokemon = 2;
	boolean spawn = true;
	
	/* PROJECTILES */
	boolean projectiles = false;
	int cooldown = 0;
	
	// lightning vars
	int lightningYpos = alienYpos[0];
	int lightningXpos;
	boolean flag = false;
	boolean lightningOn = false;
	int dropColumn = 0;
	
	
	// bubble vars
	int bubbleYpos = alienYpos[1];
	int bubbleXpos;
	boolean bubbleFlag = false;
	boolean bubbleOn = false;
	int bubbleDropColumn = 0;
	
	// bullet vars
	final int bullet_speed = 15;
	int bulletXpos = xpos;
	int bulletYpos = ypos + 65;
	boolean bulletFlight = true;
	int gridLocation = 0;
	
	// timer vars
	long startTime;
	long elapsedTime;
	long elapsedSeconds;
	
	
	// images for spites
	Image trainer1, trainer2, trainer3;
	Image [] trainer = new Image[3];
	
	// number of lives images
	Image lives1, lives2, lives3;
	Image [] displayedLives = new Image[3];
	
	// pokemon images
	Image pokeImage1, pokeImage2;
	
	// projectiles images
	Image pokeBullet, bolt, bubble;
	
	// cursor image
	Image cursorImage;
	
	// image for background of the game
	Image pokeBack, loseScreen, menu, intro, settings, defeat;
	
	// images for song titles
	Image song1, song2, song3, song4;
	
    
	/* used from the connect 4 assignment */
    // For drawing images offScreen (prevents Flicker)
    // These variables keep track of an off screen image object and
    // its corresponding graphics object
    Image offScreenImage;
    Graphics offScreenBuffer;
    
	// Constructor
	public PokemonInvaders () {
		// setting up the screen
		setPreferredSize (new Dimension (width, length));
		setLocation (1080, 1080);
		setBackground (new Color (255, 255, 255));
		setFont(new Font("Arial", Font.BOLD, 25));
		
		//starting the thread
		thread = new Thread(this);
		thread.start();
		
		// loading all the pictures in the game
		MediaTracker tracker = new MediaTracker (this);
		
		/* Game Images */
		
		// trainer images
		trainer1 = Toolkit.getDefaultToolkit ().getImage ("dawn.png");
		tracker.addImage(trainer1, 0);
		
		trainer2 = Toolkit.getDefaultToolkit ().getImage ("lucas.png");
		tracker.addImage(trainer2, 1);
		
		trainer3 = Toolkit.getDefaultToolkit ().getImage ("cynthia.png");
		tracker.addImage(trainer3, 2);
		
		// pokemon sprite images
		pokeImage1 = Toolkit.getDefaultToolkit ().getImage ("pika.png");
		pokeImage1 = pokeImage1.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
		tracker.addImage (pokeImage1, 3);
		
		pokeImage2 = Toolkit.getDefaultToolkit ().getImage ("plip.png");
		pokeImage2 = pokeImage2.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
		tracker.addImage (pokeImage2, 4);
		
		// bullet image
		pokeBullet = Toolkit.getDefaultToolkit ().getImage ("poke.png");
		pokeBullet = pokeBullet.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		tracker.addImage (pokeBullet, 5);
		
		// background of the game
		pokeBack = Toolkit.getDefaultToolkit ().getImage ("pokeBack.png");
		pokeBack = pokeBack.getScaledInstance(width, length, Image.SCALE_SMOOTH);
		tracker.addImage (pokeBack, 6);
		
		/* Screen Images */
		loseScreen = Toolkit.getDefaultToolkit ().getImage ("screens/defeat.png");
		loseScreen = loseScreen.getScaledInstance(width, length, Image.SCALE_SMOOTH);
		tracker.addImage (loseScreen, 7);
		
		menu = Toolkit.getDefaultToolkit ().getImage ("screens/menu.png");
		menu = menu.getScaledInstance(width, length, Image.SCALE_SMOOTH);
		tracker.addImage (menu, 8);
		
		intro = Toolkit.getDefaultToolkit ().getImage ("screens/howToPlay.png");
		intro = intro.getScaledInstance(width, length, Image.SCALE_SMOOTH);
		tracker.addImage (intro, 9);
		
		settings = Toolkit.getDefaultToolkit ().getImage ("screens/settings.png");
		settings = settings.getScaledInstance(width, length, Image.SCALE_SMOOTH);
		tracker.addImage (settings, 10);
		
		/* Eneemy bullets */
		bolt = Toolkit.getDefaultToolkit ().getImage ("lightling.png");
		bolt = bolt.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		tracker.addImage (bolt, 11);

		bubble = Toolkit.getDefaultToolkit ().getImage ("bubble.png");
		bubble = bubble.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		tracker.addImage (bubble, 12);
		
		/* Song Titles */
		song1 = Toolkit.getDefaultToolkit ().getImage ("names/national park.png");
		tracker.addImage (song1, 13);
		
		song2 = Toolkit.getDefaultToolkit ().getImage ("names/pokemon league night-time.png");
		song2 = song2.getScaledInstance(400, 70, Image.SCALE_SMOOTH);
		tracker.addImage (song2, 14);
		
		song3 = Toolkit.getDefaultToolkit ().getImage ("names/snowbelle city theme.png");
		song3 = song3.getScaledInstance(400, 70, Image.SCALE_SMOOTH);
		tracker.addImage (song3, 15);
		
		song4 = Toolkit.getDefaultToolkit ().getImage ("names/battle! cynthia music.png");
		song4 = song4.getScaledInstance(400, 70, Image.SCALE_SMOOTH);
		tracker.addImage (song4, 16);
		
		/* Numbers of Lives Images */
		lives1 = Toolkit.getDefaultToolkit ().getImage ("names/1.png");
		tracker.addImage (lives1, 17);
		
		lives2 = Toolkit.getDefaultToolkit ().getImage ("names/2.png");
		tracker.addImage (lives2, 18);
		
		lives3 = Toolkit.getDefaultToolkit ().getImage ("names/3.png");
		tracker.addImage (lives3, 19);
		
		//  Wait until all of the images are loaded
		try{
		    tracker.waitForAll ();
		}
		catch (InterruptedException e) {
			
		}
		
		// storing all trainer images into an array
		trainer = new Image[] {trainer1, trainer2, trainer3};
		
		// storing all the lives images into an array
		displayedLives = new Image[] {lives1, lives2, lives3};
		
		// storing all the names into an array
		playlistSongs = new Image[] {song1, song2, song3, song4};
		
		setFocusable (true); // Need this to set the focus to the panel in order to add the keyListener
		addKeyListener (this);
		addMouseListener(this);
		
		/* Audio Files */
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File ("music/national park.wav"));
			nationalPark = AudioSystem.getClip();
			nationalPark.open(sound);
			
			sound = AudioSystem.getAudioInputStream(new File ("music/snowbelle theme.wav"));
			snowbelleTheme = AudioSystem.getClip();
			snowbelleTheme.open(sound);
			
			sound = AudioSystem.getAudioInputStream(new File ("music/losing theme.wav"));
			losingSong = AudioSystem.getClip();
			losingSong.open(sound);
			
			sound = AudioSystem.getAudioInputStream(new File ("music/cynthia theme.wav"));
			cynthiaTheme = AudioSystem.getClip();
			cynthiaTheme.open(sound);
			

		}
		catch (Exception e){
			
		}
		
		// source used to place clips into an array
		// https://stackoverflow.com/questions/10520617/why-can-array-constants-only-be-used-in-initializers
		playlist = new Clip [] {nationalPark, losingSong, snowbelleTheme, cynthiaTheme};
		
		menuScreen = true;
		
		// Adding a custom mouse to the game
		cursorImage = Toolkit.getDefaultToolkit().getImage("cursor.png");
		cursorImage = cursorImage.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		Toolkit toolkit = Toolkit.getDefaultToolkit ();
		Cursor cursor = toolkit.createCustomCursor (cursorImage, new Point(0,0), "pen");
		frame.setCursor (cursor);

	}
	
	// paintComponent method, paints all the images of the game
    // Parameters: takes the graphics object
    // Return: returns no value
	public void paintComponent (Graphics g){
		// Set up the offscreen buffer the first time paint() is called
		if (offScreenBuffer == null) {
			    offScreenImage = createImage (this.getWidth (), this.getHeight ());
			    offScreenBuffer = offScreenImage.getGraphics ();	    
		}
		// Screens of the game
		menuScreen(g);
		settingScreen(g);
		instructionScreen(g);
		
		// actual game
		game(g);
		
		// checks if the user lost the game
		loseScreen(g);
    }
	
	
	
    /* Start Up Methods */
	
	// newGame method, allows the user to enter the game (generates the game and set the lives)
    // Parameters: takes in no parameters
    // Return: returns no value
    public void newGame () {
    	// setting lives to 3
		lives = 3;
		
		// switching screens
		gameOver = false;
		onScreen = false;
		
		// enemy bullets
		projectiles = true;
		lightningOn = true;
		bubbleOn = true;
		bubbleFlag = true;
		flag = true;
		
		// number of pokemons
		rowsOfPokemon = 2;
		
		// score
		score = 0;
		
		// setting the characters position
		xpos = length/2 - 40;
		ypos = 735;
		
		// alien drawing
		spawn = true;
		initialize();
		repaint ();
    }
	
	// run method, constantly being ran as a loop to update images on the screen
    // Parameters: takes in no parameters
    // Return: returns no value
	@Override
	public void run() {
		initialize();
		// starting timer
		startTime = System.currentTimeMillis();
		while(true) {
			//updating graphics methods
			updateBullet();
			updateAliens();
			updateBubble();
			updateLightning();
			this.repaint();
			try {
				Thread.sleep(1000/fps);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
		 
	// initialize method, sets the x positions of all the aliens
	// Parameters: takes in no parameters
	// Return: returns no value
	public void initialize() {
		if (spawn) {
			// setting up the xPos for each of the aliens
			// setting up the number of aliens in each column
			for (int i = 0; i < 15; i++) {
				alienXpos[0] = 0;
				alienXpos[i+1] = 70 + alienXpos[i];
				alienColumnCount[i] = rowsOfPokemon;
			}
			// setting up the yPos for each alien
			for (int i = 0; i < rowsOfPokemon; i++) {
				alienYpos[0] = 0;
				alienYpos[i+1] = alienYpos[i] + characterSize;
			}
			spawn = false;
		}
	}
	
	
	
	/* MAIN GAME METHOD */
	
	// game method, the actual game is drawn here
	// Parameters: takes in the graphics object
	// Return: returns no value
	public void game(Graphics g) {
		// actual game
		if (!gameOver && !onScreen) {
			// animates the background
			offScreenBuffer.clearRect (0, 0, this.getWidth (), this.getHeight ());
			offScreenBuffer.drawImage (pokeBack, 0, 0, this);
			// Transfer the offScreenBuffer to the screen
			g.drawImage (offScreenImage, 0, 0, this);
			
			// drawing the number of lives the user has
			offScreenBuffer.drawImage (displayedLives[lives - 1], 0, length - 32, this);
			g.drawImage (offScreenImage, 0, 0, this);
			
			// animates the trainer
			offScreenBuffer.drawImage (trainer[trainerOfChoice], xpos, ypos, this);
			g.drawImage (offScreenImage, 0, 0, this);
		
			
			/* Drawing Aliens and Bullet */
			drawAliens(g);
			drawBullet(g);
			
			/* Projectiles methods  */
			drawLightning(g);
			drawBubble(g);
			
			/* IN GAME TIMER */
			elapsedTime = System.currentTimeMillis() - startTime;
			elapsedSeconds = (elapsedTime / 1000) + 1;
			
			// checks aliens
			checkAliens();
			// checks if the user lost
			loseCheck();
		}
	}
	
	
	
	/* Drawing graphics methods */
	
	// drawBullet method, draws the bullet at its given positions
	// Parameters: takes in the graphics object
	// Return: returns no value
	public void drawBullet(Graphics g) {
		// if space is pressed, draw the bullet
		if (bulletFlight) {
			// if the bullet reaches to the top of the screen, stop drawing it
			if (bulletYpos > -80){
				// if the bullet connects with the alien, remove the bullet from the screen
				boolean check = detection();
				if (!check) {
					offScreenBuffer.drawImage (pokeBullet, bulletXpos, bulletYpos, this);
					g.drawImage (offScreenImage, 0, 0, this);
				}
			}
			else {
				// set the boolean to be false to shoot another bullet
				bulletFlight = false;
				// relocate the bullet Y-pos
				bulletYpos = ypos + 50;
			}
		}
	}
	
	// drawAliens method, draws the aliens at its given positions
	// Parameters: takes in the graphics object
	// Return: returns no value
	public void drawAliens(Graphics g) {
		// if the alien has not reached the end, continue drawing
		if (alienYpos[0] < length) {
			// animates the aliens as a grid
			for(int j = 0; j < rowsOfPokemon; j++) {
				// prints the row of aliens
				for (int i = 0; i < 15; i++) {
					// if there are aliens in the column print it
					if (alienColumnCount[i] > 0){
						// if the alien is already taken down, do not draw it
						if (alienColumnCount[i] != j && alienColumnCount[i] > j){
							// changes the pokemon for different rows
							if(j % 2 == 0)
								offScreenBuffer.drawImage (pokeImage1, alienXpos[i], alienYpos[j], this);
							else
								offScreenBuffer.drawImage (pokeImage2, alienXpos[i], alienYpos[j], this);
						} 
					}
				}
			}
			// draws the image on the screen
			g.drawImage (offScreenImage, 0, 0, this);
		}
	}
	
	// drawLightning method, draws the the bolt of lightning that interacts with the user
	// Parameters: takes in the graphics object
	// Return: returns no value
	public void drawLightning(Graphics g) {
		// generate a random coordinate for the bolt to spawn in
		if (flag) {
			while (true) {
				dropColumn = (int)(Math. random() * 15);
				
				// if that column has a valid alien to shoot out from, shoot the projectile
				if (alienColumnCount[dropColumn] > 0) {
					lightningOn = true;
					break;
				}
				
				// if that column does not have a valid alien to shoot from, do not draw the projectile
				else if (alienColumnCount[dropColumn] <= 0) {
					lightningOn = false;
					break;
				}
			}
		}
		
		flag = false;
		// added the valid x position as the lightning position
		lightningXpos = alienXpos[dropColumn] + 20; // added 20 to relocate to the middle 
		
		// draw the bolt only if the game is on
		if (projectiles && !gameOver && lightningOn) {	
			// checks to see if the user got hit by the bolt, if so stop drawing
			boolean hit = lightningHit();
			// drawing the bolt
			if (lightningYpos < length && !hit) {
				offScreenBuffer.drawImage (bolt, lightningXpos, lightningYpos, this);
				g.drawImage (offScreenImage, 0, 0, this);
			}
			// if the bolt leaves the screen, reset
			else {
				flag = true;
				lightningOn = false;
			}
		}
	}
	
	// drawBubble method, draws the bubble that interacts with the user
	// Parameters: takes in the graphics object
	// Return: returns no value
	public void drawBubble(Graphics g) {
		// generate a random coordinate for the bubble to spawn in
		if (bubbleFlag) {
			while (true) {
				bubbleDropColumn = (int)(Math. random() * 15);
				
				// if that column has a valid alien to shoot out from, shoot the projectile
				if (alienColumnCount[bubbleDropColumn] > 1) {
					bubbleOn = true;
					break;
				}
				
				// if that column does not have a valid alien to shoot from, do not draw the projectile
				else if (alienColumnCount[bubbleDropColumn] <= 1) {
					bubbleOn = false;
					break;
				}
			}
		}
		bubbleFlag = false;
		// added the valid x position as the bubble position
		bubbleXpos = alienXpos[bubbleDropColumn] + 20;

		// draw the bubble only if the game is on
		if (projectiles && !gameOver && bubbleOn) {	
			// checking if the user got hit
			boolean hit = bubbleHit();
			// drawing the bubble
			if (bubbleYpos < length && !hit) {
				offScreenBuffer.drawImage (bubble, bubbleXpos, bubbleYpos, this);
				g.drawImage (offScreenImage, 0, 0, this);
			}
			// if the bubble leaves the screen, reset
			else {
				bubbleFlag = true;
				bubbleOn = false;
			}
		}
	}

	
	
	/* Graphics update methods*/
	
	// updateBullet method, updates the position of the bullet
	// Parameters: takes in no parameters
	// Return: returns no value
	public void updateBullet() {
		// if there is a bullet in flight, update the yPos of it
		if (bulletFlight) {
			// animation to let the bullet move up constantly
			bulletYpos -= bullet_speed;	
		}
	}

	// updateBubble method, updates the position of the bubble
	// Parameters: takes in no parameters
	// Return: returns no value
	public void updateBubble() {
		if(projectiles && bubbleOn && bubbleYpos < length) {
			bubbleYpos += bullet_speed/3;
		}
		// reset the position and enable the flag
		else {
			bubbleFlag = true;
			bubbleYpos = alienYpos[1];
		}
		
	}
	
	// updateLighting method, updates the position of the bolt
	// Parameters: takes in no parameters
	// Return: returns no value
	public void updateLightning() {
		if (projectiles && lightningOn && lightningYpos < length) {
			lightningYpos += bullet_speed/3;
		}
		// reset the position and enable the flag
		else {
			flag = true;
			lightningYpos = alienYpos[0];
		}
	}
	
	// updateAliens method, updates the position of the alien on the screen
	// Parameters: takes in no parameters
	// Return: returns no value
	public void updateAliens() {
		if (!gameOver) {
			// update the y pos for every 30 seconds
			if (elapsedSeconds % 30 == 0) {
				// updating the array that contains all the x positions of the aliens
				for (int i = 0; i < rowsOfPokemon; i++) {
						alienYpos[i] += dropSpeed;
				}
			}
		}
	}
	
	
	
	/* Collision detection methods */
	
	// detection method, checks if the alien and bullet made contact, if so don't draw them anymore
	// Parameters: takes in no parameters
	// Return: returns a boolean
	public boolean detection() {
		// finds which grid of the game the bullet is currently on
		gridLocation = (int) Math.round(bulletXpos / characterSize);
		
		// if there are not aliens in that column, allow the bullet to fly to the top
		if (alienColumnCount[gridLocation] == 0) {
			return false;
		}
		
		// if the bullet's x pos is within the alien's designated grid location
		// and the bullet's y pos is less than the alien's y pos
		if (bulletXpos >= alienXpos[gridLocation] && bulletYpos < alienYpos[alienColumnCount[gridLocation] - 1] && alienColumnCount[gridLocation] > -1) {
			// change the number of aliens in the column
			alienColumnCount[gridLocation] -= 1;
			// change the boolean flight to off so the user can launch another bullet
			bulletFlight = false;
			// set the bullet's y pos back to its default
			bulletYpos = ypos + 50;
			// adding to score
			score += 50;
			return true;
		}
		else
			return false;
	}
	
	//lightningHit method, checks if the bolt hit the trainer, if it did -1 life and restart
	// Parameters: takes in no parameters
	// Return: returns a boolean
	public boolean lightningHit() {
		if(projectiles) {
			// if the lightning xpos is between the trainers xpos and its xpos + character size, render it as a hit
			// the lightning y position must be greater than the trainer's y position
			if (((lightningXpos >= xpos && lightningXpos <= xpos + characterSize/2) 
				|| (lightningXpos + 30 <= xpos + characterSize/2 && lightningXpos + 30 > xpos)) 
				&& ypos < lightningYpos && lightningOn) {
				
				// taking a life
				lives -= 1;		
				// resetting position
				lightningYpos = alienYpos[0];
				return true;
			}
		}
		return false;
	}
	
	//bubbleHit method, checks if the bubble hit the trainer, if it did -1 life and restart
	// Parameters: takes in no parameters
	// Return: returns a boolean
	public boolean bubbleHit() {
		if (projectiles) {
			// if the bubble xpos is between the trainers xpos and its xpos + character size, render it as a hit
			// the bubble y position must be greater than the trainer's y position
			if (((bubbleXpos >= xpos && bubbleXpos <= xpos + characterSize/2) 
				|| (bubbleXpos + 30 <= xpos + characterSize/2 && bubbleXpos + 30 > xpos)) 
				&& ypos < bubbleYpos && bubbleOn) {
				// taking a life
				lives -= 1;
				// resetting position
				bubbleYpos = alienYpos[1];
				return true;
			}
		}
		return false;
	}
	
	
	
	/* GAME CHECKING METHODS */
	
	// loseCheck method, checks if the user will lose the game if the aliens reaches the end
	// Parameters: takes in no parameters
	// Return: returns no value
	public void loseCheck() {
		// loops over the array loop containing the number of pokemons in each column
		int max = 0;
		for (int i = 0; i < 15; i++) {
			// finding the column with the most pokemon
			if(alienColumnCount[i] >= max) {
				max = alienColumnCount[i];
			}
		}
		
		// if the pokemon reaches the end of the screen, the player loses
		if (alienYpos[max -1] > length - characterSize) {
			// set lives to be 0 and gameOver to be true;
			lives = 0;
			gameOver = true;
			
		}				
		// resetting max if there was no collision yet
		max = 0;
		
		if (lives == 0) {
			gameOver = true;
		}
	}
	
	// checkAliens method, checks if there are any aliens on the screen
	// Parameters: takes in no parameters
	// Return: returns a boolean
	public boolean checkAliens() {
		// checking the array to see if there are still aliens
		for (int i = 0; i < 15; i++) {
			if (alienColumnCount[i] != 0) {
				return false;
			}
		}
		// add a new row for complexity
		rowsOfPokemon += 1;
		// resetting the timer for the aliens to drop later
		elapsedSeconds = 0;
		// reset the aliens back to the top
		spawn = true;
		initialize();
		return true;
	}
	
	
	
	/* SCREEN METHODS */
	
    // menu method, sends the user to the menu page
    // Parameters: takes in the graphics object
    // Return: returns no value
    public void menuScreen(Graphics g) {
    	// the start of the game screen
		if (menuScreen) {
			// drawing the menu screen
			offScreenBuffer.drawImage (menu, 0, 0, this);
			g.drawImage (offScreenImage, 0, 0, this);
			
			// opening file to check the high score
			try {
				displayHighscore();
			} catch (FileNotFoundException e) {
				
			}
			
			// displaying high score
			String displayHighscore = Integer.toString(highscore);
			g.drawString(displayHighscore, 25, 280);
			
			// playing music in the beginning
			introMusic();
			
		}
    }
    
    // instructionScreen, sends the user to the instruction page
    // Parameters: takes in the graphics object
    // Return: returns no value
    public void instructionScreen(Graphics g) {
    	// the instruction screen
		if (instructionScreen) {
			offScreenBuffer.drawImage (intro, 0, 0, this);
			g.drawImage (offScreenImage, 0, 0, this);
		}
    }
    
    // settingScreen, sends the user to the setting page
    // Parameters: takes in the graphics object
    // Return: returns no value
    public void settingScreen(Graphics g) {
    	if (settingScreen) {
    		// drawing settings screen
			offScreenBuffer.drawImage (settings, 0, 0, this);
			g.drawImage (offScreenImage, 0, 0, this);
			
			// drawing the song title for the current playing song
			g.drawImage(playlistSongs[currentSong], 310, 450, this);
			
			// drawing the trainer for the user to choose from
			g.drawImage(trainer[trainerOfChoice], 466, 657, this);
    	}
    }
    
    // loseScreen, sends the user to the losing screen page
    // Parameters: takes in the graphics object
    // Return: returns no value
    public void loseScreen(Graphics g) {
		// checking if the user loss, if so display a losing screen and a play again feature
		if (gameOver && lives == 0) {
			offScreenBuffer.drawImage (loseScreen, 0, 0, this);
			// Transfer the offScreenBuffer to the screen
			g.drawImage (offScreenImage, 0, 0, this);
			
			// drawing the user's score
			String displayScore = Integer.toString(score);
			g.drawString(displayScore, 25, 280);
			
			// checking to see if the score is a new high score
			try {
				newHighscore();
			}
			catch(IOException e){
				
			}
		}
    }
    
    
    /* HIGH SCORE METHODS */ 
    
    // newHighscore method, checks the user's current score and compares it to the high score
    // Parameters: takes nothing
    // Return: returns nothing
    public void newHighscore () throws IOException {
    	if (gameOver && score >= highscore) {
    		try {
    			// reading 
        		PrintWriter outFile = new PrintWriter(new FileWriter("highscore.txt"));
        		outFile.print(score);
        		outFile.close();
        		displayed = true;
    		}
    		catch(IOException e) {
    			
    		}
    	}
    }
    
    // displayHighscore method, checks the highscore.txt file and set the high score to be the first line
    // Parameters: takes nothing
    // Return: returns nothing
    public void displayHighscore() throws FileNotFoundException {
    	if (displayed) {
        	try {
        		Scanner inFile = new Scanner (new File ("highscore.txt"));
            	// reads all the lines of the file
            	while (inFile.hasNextLine()){
            		// stores the high score
            	  	 highscore = Integer.parseInt(inFile.nextLine());
            	}
            	// close the file
            	inFile.close();
            	displayed = false;
        	}
        	catch(FileNotFoundException e) {
        		
        	}
    	}
    }
	
	/* AUDIO METHODS */
    
    // introMusic method, checks the boolean music to determine if music is played or not
    // Parameters: takes nothing
    // Return: returns nothing
    public void introMusic() {
    	if (!music) {
    		// resetting to the start of the song
    		playlist[currentSong].setFramePosition (0);
    		playlist[currentSong].stop();
    	}
    	
    	else {
        	playlist[currentSong].loop(Clip.LOOP_CONTINUOUSLY);
    	}
    }
    
     
	/* INPUT METHODS FOR THE GAME */
    
	public void actionPerformed(ActionEvent e) {
		return;
	}
	
	
	/* KEY LISTENER METHODS */
	
	// keyPressed method, used to control the trainer and launch the bullet
	// Parameters: takes in KeyEvent kp
	// Return: returns nothing
	public void keyPressed (KeyEvent kp) {
		if(!gameOver && lives > 0) {
			// lets the trainer move left and right with arrow keys
			if (kp.getKeyCode () == KeyEvent.VK_RIGHT) {
				// checking to see if they can move (prevent moving off the screen)
				if (xpos < 1010) {
					xpos += 10;
				}
			}
			
			else if (kp.getKeyCode () == KeyEvent.VK_LEFT) {
				// checking to see if they can move (prevent moving off the screen)
				if (xpos > 0) {
					xpos -= 10;
				}
			}
			
			else if (kp.getKeyCode () == KeyEvent.VK_SPACE) {
				// if there is no bullet of screen, set the flight to be true and set the xpos
				// to the trainer's current xpos
				if (!bulletFlight) {
					bulletFlight = true;
					bulletXpos = xpos;
				}
			}
			
			else
			    return;
			
			repaint ();
		}
	}
	
	public void keyTyped(KeyEvent e) {
	}
	
	public void keyReleased(KeyEvent e) {
	}

    
	
	/* MOUSE LISTENER METHODS */
	
	// mouseClicked method, used to navigate through the screens of the game
	// Parameters: takes in MouseEvent
	// Return: returns no value
	public void mouseClicked (MouseEvent e) {
		if (menuScreen || instructionScreen || settingScreen || gameOver) {
			x = e.getX ();
			y = e.getY ();
			/* the screens within the game */
			// the menu screen
			if (menuScreen) {
				if (x >= 465 && x <= 600 && y >= 266 && y <= 332) {
					menuScreen = false;
					newGame();
					repaint();
				}
				
				// sending users to settings
				else if (x >= 411 && x <= 647 && y >= 462 && y <= 515) {
					menuScreen = false;
					settingScreen = true;
					repaint();
				}
				
				// sending users to instructions
				else if (x >= 369 && x <= 726 && y >= 363 && y <= 449) {
					menuScreen = false;
					instructionScreen = true;
					repaint();
				}
				
				// letting the user quit the game
				else if (x >= 448 && x <= 604 && y >= 552 && y <= 634) {
				    System.exit (0);
				}
			}
			
			// the settings screen
			else if (settingScreen) {
				// if the user clicks on return to menu, send them back
				if (x >= 3 && x <= 170 && y >= 0 && y <= 32) {
					settingScreen = false;
					menuScreen = true;
					repaint();
				}
				
				// turning on the music from settings
				else if (x >= 517 && x <= 623 && y >= 240 && y <= 305) {
					music = true;
					introMusic();
				}
				
				// turning off the music from settings
				else if (x >= 711 && x <= 845 && y >= 240 && y <= 310) {
					music = false;
					introMusic();
				}
				
				// moving forward through the array to switch songs
				else if (x >= 741 && x <= 805 && y >= 461 && y <= 510) {
					if(currentSong < 3) {
						currentSong += 1;
			    		playlist[currentSong - 1].setFramePosition (0);
						playlist[currentSong -1].stop();
						introMusic();
					}
				}
				
				// moving backward through the array to switch songs
				else if (x >= 215 && x <= 286 && y >= 453 && y <= 510) {
					if(currentSong > 0) {
						currentSong -= 1;
			    		playlist[currentSong + 1].setFramePosition (0);
						playlist[currentSong + 1].stop();
						introMusic();
					}
				}
				
				// lets the user to choose which trainer they want to use (moving right)
				else if (x >= 740 && x <= 805  && y >= 657  && y <= 722 ) {
					if (trainerOfChoice < 2){
						trainerOfChoice += 1;
					}
				}
				
				// lets the user to choose which trainer they want to use (moving left)
				else if (x >= 215 && x <= 275 && y >= 658 && y <= 717) {
					if (trainerOfChoice > 0) {
						trainerOfChoice -= 1;
					}
				}
				
				
			}
			
			// the instruction screen
			else if (instructionScreen) {
				// if the user clicks on return to menu, send them back
				if (x >= 3 && x <= 170 && y >= 0 && y <= 32) {
					instructionScreen = false;
					menuScreen = true;
					repaint();
				}
			}
			
			// losing screen interface
			if (gameOver && lives == 0) {
				// making rectangles to take in the input from the user's mouse
				if (x >= 177 && x <= 469 && y >= 400 && y <= 490) {
					newGame();
				}
				
				// sends the user back to the menu screen
				else if (x >= 118 && x <= 522 && y >= 518 && y <= 600) {
					menuScreen = true;
					lives = 3;
				}
				
				// quits the game
				else if (x >= 262 && x <= 377 && y >= 637 && y <= 700) {
					System.exit(0);
				}
			}
		}
	 }
	
	
	public void mouseReleased (MouseEvent e) {
	}
	
	
	public void mouseEntered (MouseEvent e) {
	}
	
	
	public void mouseExited (MouseEvent e) {
	} 
	
	
	public void mousePressed (MouseEvent e) {
	}
	 
	   
	public void update (Graphics g) {
		paint (g);
	}
   

	
	public static void main(String[] args) {
		frame = new JFrame ("Pokemon Invaders");
		PokemonInvaders myPanel = new PokemonInvaders ();
		frame.add (myPanel);
		frame.pack ();
		frame.setVisible (true);
	}

}