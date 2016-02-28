
//Ivan Craddock
//Ninja Bunny Homework

package ninja.bunny;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.MoveXModifier;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.Toast;


public class NinjaBunny extends BaseGameActivity implements IOnSceneTouchListener {

	//declare Objects for later use
	private Camera mCamera;
	private Scene mMainScene;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TextureRegion mPlayerTextureRegion;
	private Sprite player;
	private TextureRegion mTargetTextureRegion;
	private LinkedList<Sprite> targetLL;
	private LinkedList<Sprite> TargetsToBeAdded;
	private LinkedList<Sprite> projectileLL;
	private LinkedList<Sprite> projectilesToBeAdded;
	private TextureRegion mProjectileTextureRegion;
	private Sound shootingSound;
	private Music backgroundMusic;
	private TextureRegion mPausedTextureRegion;
	private CameraScene mPauseScene;
	private CameraScene mResultScene;
	private boolean runningFlag = false;
	private boolean pauseFlag = false;
	private BitmapTextureAtlas mFontTexture;
	private Font mFont;
	private ChangeableText score;
	private int hitCount;
	private final int maxScore = 10;
	private Sprite winSprite;
	private Sprite failSprite;
	private TextureRegion mWinTextureRegion;
	private TextureRegion mFailTextureRegion;	
		
	//unused method
	public void onLoadComplete() {}
	
	//Fixes display to phone screen dimensions in the landscape position
	public Engine onLoadEngine() {
		final Display display = getWindowManager().getDefaultDisplay();
		int cameraWidth = display.getWidth();
		int cameraHeight = display.getHeight();
		mCamera = new Camera(0,0,cameraWidth,cameraHeight);
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera).setNeedsMusic(true).setNeedsSound(true));
	}

	//Fixes resource files to class objects declared earlier
	public void onLoadResources(){
		mBitmapTextureAtlas = new BitmapTextureAtlas(512,512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ninjabunny.png", 0,0);
		mTargetTextureRegion = BitmapTextureAtlasTextureRegionFactory
			    .createFromAsset(this.mBitmapTextureAtlas, this, "Target.png",
			    128, 0);
		mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);
		mProjectileTextureRegion = BitmapTextureAtlasTextureRegionFactory
			    .createFromAsset(this.mBitmapTextureAtlas, this,
			    "Projectile.png", 64, 0);
		SoundFactory.setAssetBasePath("mfx/");
		try {
		    shootingSound = SoundFactory.createSoundFromAsset(mEngine
		        .getSoundManager(), this, "pew_pew_lei.wav");
		} catch (IllegalStateException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}

		MusicFactory.setAssetBasePath("mfx/");
		try {
		    backgroundMusic = MusicFactory.createMusicFromAsset(mEngine
		        .getMusicManager(), this, "background_music.ogg");
		    backgroundMusic.setLooping(true);
		} catch (IllegalStateException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		//loads resources into engine
		mPausedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "paused.png",0, 64);
		mWinTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "win.png", 0,128);
		mFailTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "fail.png", 0,256);
		mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);
		mFontTexture = new BitmapTextureAtlas(256, 256,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 40, true, Color.BLACK);
		mEngine.getTextureManager().loadTexture(mFontTexture);
		mEngine.getFontManager().loadFont(mFont);

	}
	
	//Loads assets into screen/sound player, directs those assets in accordance with user activity
	//calls sub-methods
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		mMainScene = new Scene();
		mMainScene.setBackground(new ColorBackground(0.9274f, 0.8784f, 0.8784f));
		final int PlayerX = this.mPlayerTextureRegion.getWidth()/2;
		final int PlayerY = (int) ((mCamera.getHeight()-mPlayerTextureRegion.getHeight())/2);
		player = new Sprite(PlayerX, PlayerY, mPlayerTextureRegion);
		player.setScale(2);
		mMainScene.attachChild(player);
		targetLL = new LinkedList<Sprite>();
		TargetsToBeAdded = new LinkedList<Sprite>();
		createSpriteSpawnTimeHandler();
		mMainScene.registerUpdateHandler(detect);
		projectileLL = new LinkedList<Sprite>();
		projectilesToBeAdded = new LinkedList<Sprite>();
		mMainScene.setOnSceneTouchListener(this);
		backgroundMusic.play();
		mPauseScene = new CameraScene(mCamera);
		final int x = (int) (mCamera.getWidth() / 2 - mPausedTextureRegion.getWidth() / 2);
		final int y = (int) (mCamera.getHeight() / 2 - mPausedTextureRegion.getHeight() / 2);
		final Sprite pausedSprite = new Sprite(x, y, mPausedTextureRegion);
		mPauseScene.attachChild(pausedSprite);
		mPauseScene.setBackgroundEnabled(false);
		mResultScene = new CameraScene(mCamera);
		winSprite = new Sprite(x, y, mWinTextureRegion);
		failSprite = new Sprite(x, y, mFailTextureRegion);
		mResultScene.attachChild(winSprite);
		mResultScene.attachChild(failSprite);
		mResultScene.setBackgroundEnabled(false);
		winSprite.setVisible(false);
		failSprite.setVisible(false);
		score = new ChangeableText(0, 0, mFont, String.valueOf(maxScore));
		score.setPosition(mCamera.getWidth() - score.getWidth() - 15, 5);
		restart();
		return mMainScene;
		
	}
	
	//game over method
	public void fail() {
		   if (mEngine.isRunning()) {
		        winSprite.setVisible(false);
		        failSprite.setVisible(true);
		        mMainScene.setChildScene(mResultScene, false, true, true);
		        mEngine.stop();
		    }
		}

	//displays message if player survives
	public void win() {
		    if (mEngine.isRunning()) {
		        failSprite.setVisible(false);
		        winSprite.setVisible(true);
		        mMainScene.setChildScene(mResultScene, false, true, true);
		        mEngine.stop();
		    }
		}
	
	//method for resetting assets and restarting game
	public void restart() {

	    runOnUpdateThread(new Runnable() {

	        public void run() {
	            mMainScene.detachChildren();
	            mMainScene.attachChild(player, 0);
	            mMainScene.attachChild(score);
	        }
	    });

	    hitCount = 0;
	    score.setText(String.valueOf(hitCount));
	    projectileLL.clear();
	    projectilesToBeAdded.clear();
	    TargetsToBeAdded.clear();
	    targetLL.clear();
	}
	
	//detects user input and directs projectile object towards point selected by user
	private void shootProjectile(final float pX, final float pY) {

	    int offX = (int) (pX - player.getX());
	    int offY = (int) (pY - player.getY());
	    if (offX <= 0)
	        return;

	    final Sprite projectile;
	    projectile = new Sprite(player.getX(), player.getY(),
	    mProjectileTextureRegion.deepCopy());
	    projectile.setScale(2);
	    mMainScene.attachChild(projectile, 1);

	    int realX = (int) (mCamera.getWidth() + projectile.getWidth() / 2.0f);
	    float ratio = (float) offY / (float) offX;
	    int realY = (int) ((realX * ratio) + projectile.getY());

	    int offRealX = (int) (realX - projectile.getX());
	    int offRealY = (int) (realY - projectile.getY());
	    float length = (float) Math.sqrt((offRealX * offRealX) + (offRealY * offRealY));
	    float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
	    float realMoveDuration = length / velocity;

	    MoveModifier mod = new MoveModifier(realMoveDuration,
	    projectile.getX(), realX, projectile.getY(), realY);
	    projectile.registerEntityModifier(mod.deepCopy());

	    projectilesToBeAdded.add(projectile);
	    shootingSound.play();
	}
	
	//target spawning method
	public void addTarget() {
	    Random rand = new Random();

	    int x = (int) mCamera.getWidth() + mTargetTextureRegion.getWidth();
	    int minY = mTargetTextureRegion.getHeight();
	    int maxY = (int) (mCamera.getHeight() - mTargetTextureRegion.getHeight());
	    int rangeY = maxY - minY;
	    int y = rand.nextInt(rangeY) + minY;

	    Sprite target = new Sprite(x, y, mTargetTextureRegion.deepCopy());
	    target.setScale(2);
	    mMainScene.attachChild(target);

	    int minDuration = 2;
	    int maxDuration = 4;
	    int rangeDuration = maxDuration - minDuration;
	    int actualDuration = rand.nextInt(rangeDuration) + minDuration;

	    MoveXModifier mod = new MoveXModifier(actualDuration, target.getX(),-target.getWidth());
	    target.registerEntityModifier(mod.deepCopy());

	    TargetsToBeAdded.add(target);

	}

	//collision detector. Detects collisions betweent target and projectile or target and end of screen
	IUpdateHandler detect = new IUpdateHandler() {
	    public void reset() {
	    }

	    public void onUpdate(float pSecondsElapsed) {

	        Iterator<Sprite> targets = targetLL.iterator();
	        Sprite _target;
	        boolean hit = false;

	        while (targets.hasNext()) {
	            _target = targets.next();

	            if (_target.getX() <= -_target.getWidth()) {
	                removeSprite(_target, targets);
	                fail();
	                break;
	            }
	            Iterator<Sprite> projectiles = projectileLL.iterator();
	            Sprite _projectile;
	            while (projectiles.hasNext()) {
	                _projectile = projectiles.next();

	                if (_projectile.getX() >= mCamera.getWidth() || _projectile.getY() >= mCamera.getHeight() + _projectile.getHeight() || _projectile.getY() <= -_projectile.getHeight()) {
	                        removeSprite(_projectile, projectiles);
	                        continue;
	                }

	                if (_target.collidesWith(_projectile)) {
	                    removeSprite(_projectile, projectiles);
	                    hit = true;
	                    break;
	                }
	                if (hitCount >= maxScore) {
	                    win();
	                }
	            }
	            if (hit) {
	                removeSprite(_target, targets);
	                hit = false;
	                hitCount++;
	                score.setText(String.valueOf(hitCount));
	            }

	        }
	        projectileLL.addAll(projectilesToBeAdded);
	        projectilesToBeAdded.clear();
	        targetLL.addAll(TargetsToBeAdded);
	        TargetsToBeAdded.clear();
	    }
	};
	
	//deletes sprites from screen
	public void removeSprite(final Sprite _sprite, Iterator<Sprite> it){
		runOnUpdateThread(new Runnable(){
			public void run(){
				mMainScene.detachChild(_sprite);
			}
		});
		it.remove();
	}
	
	//halts game processes
	public void pauseGame() {
	    mMainScene.setChildScene(mPauseScene, false, true, true);
	    mEngine.stop();
	}

	//resumes game processes
	public void unPauseGame() {
	    mMainScene.clearChildScene();
	    mEngine.start();
	}

	//halts sound processes
	public void pauseMusic() {
	    if (runningFlag)
	        if (backgroundMusic.isPlaying())
	            backgroundMusic.pause();
	}

	//resumes sound processes
	public void resumeMusic() {
	    if (runningFlag)
	        if (!backgroundMusic.isPlaying())
	            backgroundMusic.resume();
	}
	@Override
	
	//calls unpause methods
	public void onResumeGame() {
	    super.onResumeGame();
	    if (runningFlag) {
	        if (pauseFlag) {
	            pauseFlag = false;
	            Toast.makeText(this, "Menu button to resume",
	            Toast.LENGTH_SHORT).show();
	        } else {
	            resumeMusic();
	            mEngine.stop();
	        }
	    } else {
	        runningFlag = true;
	    }
	}
	@Override
	
	//calls pause methods
	protected void onPause() {
	    if (runningFlag) {
	        pauseMusic();
	        if (mEngine.isRunning()) {
	            pauseGame();
	            pauseFlag = true;
	        }
	    }
	    super.onPause();
	}
	@Override
	
	//detects point selected on screen by user
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
	    if (pKeyCode == KeyEvent.KEYCODE_MENU
	        && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
	        if (mEngine.isRunning() && backgroundMusic.isPlaying()) {
	            pauseMusic();
	            pauseFlag = true;
	            pauseGame();
	            Toast.makeText(this, "Menu button to resume",
	                Toast.LENGTH_SHORT).show();
	        } else {
	            if (!backgroundMusic.isPlaying()) {
	                unPauseGame();
	                pauseFlag = false;
	                resumeMusic();
	                mEngine.start();
	            }
	            return true;
	        }
	    } else if (pKeyCode == KeyEvent.KEYCODE_BACK
	            && pEvent.getAction() == KeyEvent.ACTION_DOWN) {

	        if (!mEngine.isRunning() && backgroundMusic.isPlaying()) {
	            mMainScene.clearChildScene();
	            mEngine.start();
	            restart();
	            return true;
	        }
	        return super.onKeyDown(pKeyCode, pEvent);
	    }
	    return super.onKeyDown(pKeyCode, pEvent);
	}

	//creates projectiles
	private void createSpriteSpawnTimeHandler() {
	    TimerHandler spriteTimerHandler;
	    float mEffectSpawnDelay = 1f;

	    spriteTimerHandler = new TimerHandler(mEffectSpawnDelay, true,
	    new ITimerCallback() {

	        public void onTimePassed(TimerHandler pTimerHandler) {
	            addTarget();
	        }
	    });

	    getEngine().registerUpdateHandler(spriteTimerHandler);
	}

	
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {

	    if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
	        final float touchX = pSceneTouchEvent.getX();
	        final float touchY = pSceneTouchEvent.getY();
	        shootProjectile(touchX, touchY);
	        return true;
	    }
	    return false;
	}

}
