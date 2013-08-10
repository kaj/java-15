package se.kaj.femton;	// FemtonApp - Play 'n^2 - 1'
import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * FemtonPane är grundklassen för spelet. En panel med flyttbara
 * rutar. Rutorna numreras, men bilder hanteras inte.
 */
class FemtonPane extends Canvas implements MouseListener {

  protected int sida;
  protected int positioner[];
  protected Random random;
  private boolean done;
  private int n_moves;
  private Label message;
  protected int part_w, part_h;
  private Point pressed_pos;

    /**
     * Skapa en ny FemtonPane.
     * @param sida 
     */
  public FemtonPane(int sida, int part_w, int part_h) {
    restart(sida);
    this.part_w = part_w;
    this.part_h = part_h;
    random = new Random();
    message = new Label();
    
    addMouseListener(this);
  }

  public void restart(int sida) {
    this.sida = sida;
    positioner = new int[sida*sida];
    for(int i = 0; i < sida*sida; ++i)
      positioner[i] = i+1;
  }
    
  public void shuffle() {
    message.setText("Shuffeling. Please wait ...");
    n_moves = 0;
    Point p = findSpace();
    for(int i = 0; i<100; ++i) { 
      p.x = Math.abs(random.nextInt()) % sida;
      if(makeMove(p));
      p.y = Math.abs(random.nextInt()) % sida;
      if(makeMove(p));
    }
    message.setText("New game (shuffeled " + n_moves + " times).");
    n_moves = 0; done = false;
    repaint();
  }

  public void paint(Graphics g) {
    for(int y = 0; y < sida; ++y)
      for(int x = 0; x < sida; ++x)
	if(positioner[x + y*sida] != sida*sida)	{
	  if(!done) {
	    g.setColor(getBackground());
	    g.draw3DRect(x * part_w, y * part_h, part_w-1, part_h-1, true);
	    g.setColor(getForeground());
	  }
	  g.drawString(String.valueOf(positioner[x + y*sida]),
		       x * part_w + 6, y * part_h + 16);
	}
  }

  protected Point findSpace() {
    for(int i = 0; i < sida*sida; ++i)
      if(positioner[i] == sida*sida)
	return new Point(i%sida, i/sida);
    throw new RuntimeException("Found no space!");
  }

  protected int get(Point pos) {
    return positioner[pos.x + pos.y * sida];
  }
  private void set(Point pos, int val) {
    positioner[pos.x + pos.y * sida] = val;
  }

  protected boolean makeMove(Point pos) {
    n_moves ++;
    Point space = findSpace();
    if(pos.y == space.y && pos.x >=0 && pos.x < space.x) {
      for(Point t = new Point(space.x, space.y); !t.equals(pos); ) {
	t.translate(-1, 0);
	set(space, get(t));
	space.move(t.x, t.y);
	set(space, sida*sida);
      }
      return true;
    } else if(pos.y == space.y && pos.x > space.x && pos.x < sida) {
      for(Point t = new Point(space.x, space.y); !t.equals(pos); ) {
	t.translate(1, 0);
	set(space, get(t));
	space.move(t.x, t.y);
	set(space, sida*sida);
      }
      return true;
    } else if(pos.x == space.x && pos.y >= 0 && pos.y < space.y) {
      for(Point t = new Point(space.x, space.y); !t.equals(pos); ) {
	t.translate(0, -1);
	set(space, get(t));
	space.move(t.x, t.y);
	set(space, sida*sida);
      }
      return true;
    } else if(pos.x == space.x && pos.y > space.y && pos.y < sida) {
      for(Point t = new Point(space.x, space.y); !t.equals(pos); ) {
	t.translate(0, 1);
	set(space, get(t));
	space.move(t.x, t.y);
	set(space, sida*sida);
      }
      return true;
    }
    n_moves --;
    return false;
  }
    
  // Some noop mouse event handlers.
  public void mouseClicked(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent event) {
    pressed_pos = new Point(event.getX() / part_w, event.getY() / part_h);
    // Todo: check if the move is legal
    Graphics g = getGraphics();
    g.setColor(getBackground());
    g.draw3DRect(pressed_pos.x * part_w, pressed_pos.y * part_h, 
		 part_w-1, part_h-1, false);
  }

  /**
   * Musen är släppt. Kolla om positionen är ett riktigt drag, och gör
   * draget om det är ok.
   * Todo: kolla att musen är nedtryckt just på den positionen!  
   */
  public void mouseReleased(MouseEvent event) {
    if((event.getX() / part_w == pressed_pos.x) && 
       (event.getY() / part_h == pressed_pos.y)) {
      if(!makeMove(pressed_pos)) {
	message.setText("Illegal move");
	return;
      }
      message.setText("Made " + n_moves + " moves.");
      checkIfDone();
      repaint();
    }
  }
  
  public boolean isDone() {
    return done;
  }

  protected void checkIfDone() {
    Point t = new Point(0, 0);
    done = false;
    for(t.x = 0; t.x < sida; ++t.x)
      for(t.y = 0; t.y < sida; ++t.y)
	if(get(t) != t.x + t.y * sida + 1) return;

    message.setText("Congratulations! You solved the puzzle in " + n_moves + 
		    " moves.");
    done = true;
  }

  public Component getMessage() {
    return message;
  }


}

class FemtonImage extends FemtonPane {
  Image wholeimage;
  Image imgpart[];
  boolean paintable = false;
  boolean disable_image = false;

  protected Dimension preferred_size = new Dimension(200, 200);

  public FemtonImage(int sida, Image image) {
    super(sida,  0, 0);
    restart(image);
  }

  public void restart(Image image) {
    this.wholeimage = image;
    imgpart = null;
    int width = wholeimage.getWidth(this), 
      height = wholeimage.getHeight(this);
    if(width != -1 && height != -1) try {
      preferred_size = new Dimension(width, height);
      Container p = getParent();
      p.invalidate(); p.validate();
    } catch (NullPointerException e) {
      // This happens sometimes while reloading. Why?
    }
    try {
      fillParts();
      shuffle();
    } catch (RuntimeException e) {
      // Do nothing, imageUpdate will get called later.
    }
    repaint();
  }

  public void restart(int sida) {
    super.restart(sida);
    imgpart = null;
    try {
      fillParts();
      shuffle();
    } catch (RuntimeException e) {
      // Do nothing, imageUpdate will get called later.
    }
    repaint();
  }    
    
  protected void fillParts() {
    paintable = false;
    part_w = wholeimage.getWidth(this) / sida;
    part_h = wholeimage.getHeight(this) / sida;
    if(part_w <= 0 || part_h <= 0) throw new RuntimeException("No image yet");
    imgpart = new Image[sida*sida];
    for(int x = 0; x<sida; ++x)
      for(int y = 0; y<sida; ++y) {
	CropImageFilter cropfilter
	  = new CropImageFilter(x*part_w+1, y*part_h+1, part_w-2, part_h-2);
	imgpart[x + y*sida] = 
	  createImage(new FilteredImageSource(wholeimage.getSource(),
					      cropfilter));
      }
    paintable = true;
  }

  public void paint(Graphics g) {
    if(disable_image) { super.paint(g); return; }

    if(!paintable) {
      System.out.println("Tried to paint unpaintable Femton.");
      g.drawString("Please wait", 3, 15);
      g.drawString("Loading / processing image", 3, 30);
      return;
    }
    Point p = new Point(0,0);
    g.setColor(getBackground());
    if(isDone()) g.drawImage(wholeimage, 0, 0, this);
    else for(p.y = 0; p.y < sida; ++p.y)
      for(p.x = 0; p.x < sida; ++p.x)
	if(positioner[p.x + p.y*sida] != sida*sida) {
	  g.drawImage(imgpart[get(p)-1], p.x*part_w+1, p.y*part_h+1, this);
	  g.draw3DRect(p.x * part_w, p.y * part_h, part_w-1, part_h-1, true);
	}
  }

  /** 
   * To implement an ImageObserver. This is where we do stuff that
   * we couldn't do when we wanted to ...
   * @return true if more info is needed, false if satisfied.
   */
  public boolean imageUpdate(Image img, int infoflags,
			     int x, int y, int width, int height) {
    if(img == wholeimage) {
      if((infoflags & WIDTH) == WIDTH)   part_w = width  / sida; 
      if((infoflags & HEIGHT) == HEIGHT) part_h = height / sida;
      if((imgpart == null) && (part_w != 0) && (part_h != 0)) {
	System.out.println("imageUpdate: main image updated!");
	fillParts();
	shuffle();
	preferred_size = new Dimension(width, height);
	getParent().invalidate(); getParent().validate();
	return true;
      }
      if((infoflags & ALLBITS) == ALLBITS && isDone()) {
	System.out.println("imageUpdate: got main image");
	repaint(100);
	return true;
      }
      // System.out.println("imageUpdate: main image not completly updated");
      repaint(100);
      return true;
    }

    // Todo: only repaint the imgpart this is about!
    // If its the whole image, dont repaint at all!
    if((infoflags & ALLBITS) == ALLBITS) {
      //      System.out.println("imageUpdate: some image completed!");
      repaint(100);		// Todo: paint only this image!
      return true;
    }

    return true;
  }
  
  public Dimension getPreferredSize() {
    return preferred_size;
  }
  public Dimension getMinimumSize() {
    return preferred_size;
  }
}

public class FemtonApp extends Applet
  implements ActionListener, ItemListener {

  FemtonImage game = null;
  Image image;
  Choice select_img, select_size;
  GridBagLayout gridbag;
  
  protected void add(Component obj, GridBagConstraints cons) {
    gridbag.setConstraints(obj, cons);
    add(obj);
  }

  public void init() {
    System.out.println("Wellcome foo to the game of fifteen, by Rasmus Kaj " + 
		       "<kaj@e.kth.se>");
    try {
      String cdef = getParameter("bgcolor");
      Color c = new Color(Integer.valueOf(cdef.substring(1, 3),16).intValue(),
			  Integer.valueOf(cdef.substring(3, 5),16).intValue(),
			  Integer.valueOf(cdef.substring(5, 7),16).intValue());
      setBackground(c);
    } catch (Exception e) {
      System.out.println("Failed to set color: " + e);
    }

    // Create the imageselector before we add it, so we can use 'first_img'
    // to create the FemtonImage
    select_img = new Choice();
    StringTokenizer st = new StringTokenizer(getParameter("images"));
    String first_img = st.nextToken(); select_img.addItem(first_img);
    while(st.hasMoreTokens()) select_img.addItem(st.nextToken());

    // Now create and add stuff in the normal way.
    setLayout(gridbag = new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridheight = GridBagConstraints.RELATIVE;
    c.weightx = 1.0;
    c.weighty = 1.0;
    int sideparts = Integer.parseInt(getParameter("side"));
    add(game = new FemtonImage(sideparts,
			       getImage(getCodeBase(), first_img + ".gif")),
	c);

    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.EAST;
    add(new Label("Image:"), c);

    c.gridwidth = GridBagConstraints.REMAINDER;
    add(select_img, c);
    select_img.addItemListener(this);

    c.gridwidth = 1;
    add(new Label("Size:"), c);
    c.gridwidth = GridBagConstraints.REMAINDER;
    select_size = new Choice();
    select_size.addItem("3 × 3");    select_size.addItem("4 × 4");
    select_size.addItem("5 × 5");    select_size.addItem("6 × 6");
    select_size.addItem("7 × 7");
    select_size.select(sideparts - 3); // 3x3 is number 0, 4x4 is 1, etc
    add(select_size, c);
    select_size.addItemListener(this);

    c.gridwidth = GridBagConstraints.REMAINDER;
    Checkbox use_image = new Checkbox("Use image", null, true);
    add(use_image, c);
    use_image.addItemListener(this);
    c.gridheight = GridBagConstraints.RELATIVE;
    
    Button shuffle = new Button("Reshuffle");
    add(shuffle, c);
    shuffle.addActionListener(this);

    c.gridheight = GridBagConstraints.REMAINDER; 
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    add(game.getMessage(), c);
    validate();
  }

  public void itemStateChanged(ItemEvent event) {
    String arg = (String)event.getItem();
    if(event.getSource() == select_img) {
      
      System.out.println("Select image: " + arg);
      game.restart(getImage(getCodeBase(), arg + ".gif"));
//       event.consume();
      return;

    } else if(event.getSource() == select_size) {
      int size = select_size.getSelectedIndex() + 3;
      System.out.println("Select size: " + size + " (" + arg + ")");
      game.restart(size);
//       event.consume();
      return;

    } else if(event.getSource() instanceof Checkbox) {
      game.disable_image = !(event.getStateChange() == ItemEvent.SELECTED);
	//!((Checkbox)event.getSource()).getState();
      game.shuffle();
      System.out.println("You might think that the restarting of the game\n"+
			 "when (de)selecting Use image is a bug, but it is\n"+
			 "intended, to avoid cheating.\n\n");
//       event.consume();
      return;
    }
  }

  public void actionPerformed(ActionEvent event) {
    if(event.getActionCommand().equals("Reshuffle")) {
      game.shuffle();
//       event.consume();
      return;
    }
  }

//   public boolean action(Event event, Object arg) {
//     if(arg.equals("Reshuffle")) {
//       game.shuffle();
//       return true;

//     } else if(event.target == select_img) {
//       System.out.println("Select image: " + arg);
//       game.restart(getImage(getCodeBase(), arg + ".gif"));
//       return true;

//     } else if(event.target == select_size) {
//       int size = select_size.getSelectedIndex() + 3;
//       System.out.println("Select size: " + size + " (" + arg + ")");
//       game.restart(size);
//       return true;

//     } else if(event.target instanceof Checkbox) {
//       game.disable_image = !((Checkbox)event.target).getState();
//       game.shuffle();
//       System.out.println("You might think that the restarting of the game\n"+
// 			 "when (de)selecting Use image is a bug, but it is\n"+
// 			 "intended, to avoid cheating.\n\n");
//       return true;
//     }
//     return false;
//   }

  public void start() {
    System.out.println("*** The applet is started!");
    super.start();
  }
  public void stop() {
    System.out.println("*** The applet is stoped!");
    super.stop();
  }
  public void destroy() {
    System.out.println("*** The applet is destroyed!");
    super.destroy();
  }

}

