package fr.ul.cassebrique.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import fr.ul.cassebrique.controls.Listener;
import fr.ul.cassebrique.dataFactories.SoundFactory;
import fr.ul.cassebrique.dataFactories.TextureFactory;
import fr.ul.cassebrique.model.GameState;
import fr.ul.cassebrique.model.GameWorld;
import fr.ul.cassebrique.model.Racket;

/**
 * Created by Julien on 26/01/2018.
 */

public class GameScreen extends ScreenAdapter {
    protected SpriteBatch sb;
    protected GameWorld gw;
    private GameState gs;
    private OrthographicCamera camera;
    private Timer.Task timerTask;
    private Timer timer;
    private boolean tempo;
    private Viewport vp;

    public GameScreen(){
        timer = new Timer();
        boolean pause = false;
        Listener listener = new Listener(this);
        Gdx.input.setInputProcessor(listener);
        tempo = false;
        timerTask = new Timer.Task() {
            @Override
            public void run() {
                reboot();
            }
        };
        sb = new SpriteBatch();
        gs = new GameState();
        gw = new GameWorld(this);
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        vp = new FitViewport(TextureFactory.getTexBack().getWidth() ,TextureFactory.getTexBack().getHeight(), camera);
        vp.apply();
        camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 10);
        camera.update();
        sb.setProjectionMatrix(camera.combined);
    }

    @Override
    public void resize(int width, int height){
        vp.update(width, height);
        camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 10);
        camera.update();
        sb.setProjectionMatrix(camera.combined);
    }

    public void render(float delta){
        camera.update();
        if (gs.getState().equals(GameState.State.Running)) {
            gw.update();
            update();
            gw.draw(sb);
        }else if (gs.getState().equals(GameState.State.Pause)){
            gw.draw(sb);
        }else if (gs.getState().equals(GameState.State.Quit)){
            System.exit(0);
        }
        else {
            if (gs.getState().equals(GameState.State.BallLoss)) {
                gw.draw(sb);
                sb.begin();
                sb.draw(TextureFactory.getTexLossBall(), 300, 300);
                sb.end();
                if (!tempo) {
                    Timer.schedule(timerTask, 3);
                }
                tempo = true;
            } else if (gs.getState().equals(GameState.State.GameOver)) {
                gw.draw(sb);
                sb.begin();
                sb.draw(TextureFactory.getTexGameOver(), 300, 300);
                sb.end();
                if (!tempo) {
                    Timer.schedule(timerTask, 3);
                }
                tempo = true;
            } else if (gs.getState().equals(GameState.State.Won)) {
                gw.draw(sb);
                sb.begin();
                sb.draw(TextureFactory.getTexWin(), 300, 300);
                sb.end();
                if (!tempo) {
                    Timer.schedule(timerTask, 3);
                }
                tempo = true;
            }
        }
    }

    public OrthographicCamera getCamera(){
        return camera;
    }

    public void update(){
        if (gw.endBall()) {
            SoundFactory.getSoundPerte().play();
            gs.setState(GameState.State.GameOver);
        }
        if (gw.endWall()){
            SoundFactory.getSoundVictoire().play();
            gs.setState(GameState.State.Won);
        }
        if (gw.isGone()){
            SoundFactory.getSoundPerteBalle().play();
            gs.setState(GameState.State.BallLoss);
        }
        float l = TextureFactory.getTexBack().getWidth();
        float a = Gdx.graphics.getWidth();
        float ratio = l / a;
        float xClic = Gdx.input.getX() * ratio;
        Racket racket = gw.getRacket();
        float xRack = racket.getPosAbs() ;
        float limitMax = TextureFactory.getTexBack().getWidth() - (TextureFactory.getTexBorder().getWidth() * 2) - racket.getWidth();
        if (Gdx.input.getAccelerometerY() > 0 && !Gdx.input.isTouched() && xRack <= limitMax){
            racket.goRight(Math.abs(Gdx.input.getAccelerometerY()) / 10);
        }else if (Gdx.input.getAccelerometerY() < 0 && !Gdx.input.isTouched() && xRack >= TextureFactory.getTexBorder().getWidth()){
            racket.goLeft(Math.abs(Gdx.input.getAccelerometerY()) / 10);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && xRack >= TextureFactory.getTexBorder().getWidth()){
            racket.goLeft(1f);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && xRack <= limitMax) {
            racket.goRight(1f);
        }else if (xClic < xRack  && xRack > TextureFactory.getTexBorder().getWidth()&& Gdx.input.isTouched()) {
            racket.goLeft(1f);
        } else if (xClic > xRack + racket.getWidth() && xRack <= limitMax && Gdx.input.isTouched()) {
            racket.goRight(1f);
        }
    }

    public void reboot(){
        gw.reboot(gs.getState());
        gs.setState(GameState.State.Running);
        tempo = false;
    }

    public void setState(GameState.State state){
        gs.setState(state);
    }

    public GameState.State getState(){
        return gs.getState();
    }

    public void dispose () {
        sb.dispose();
    }
}
