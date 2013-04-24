import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;


public class MET extends JFrame
implements ActionListener, MouseListener {


static private int ROWS, SIZEX, SIZEY, ADD=30, BSIZE=100, BLIM;
static private String LN;
static private File fdata;

static private Color clr = new Color(240,240,240);
static private String[] head = new String[] {"x", "y", "x^2", "xy", "y^2"};

//======Components=======
private JTable tab;
private JTextArea jta;
private JLabel lab;
private Container cnt;
private JScrollPane scr, scr2, scr3;

private JMenu menu, pmenu[];
private JMenuBar mbar;
private JPopupMenu pop;
private JMenuItem mi[], pmi[][];


//======Variables=======
private double a, b, da, db, r;
private Object s;
private Object copied[][];

private GridBagConstraints c = new GridBagConstraints();


private MET() {
	
	super("Μέθοδος Ελαχίστων Τετραγώνων");
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setSize(SIZEX,SIZEY);
	
	int i;
 //create the menu
	mbar = new JMenuBar();
	menu = new JMenu("Μενού");
	mi = new JMenuItem[] { new JMenuItem("Περί..."), new JMenuItem("Βοήθεια") };
	menu.add(mi[0]);
	menu.add(mi[1]);
	mbar.add(menu);

 //create other elements
	tab = new JTable(ROWS, 5);
	jta = new JTextArea(6,20);
	lab = new JLabel("aaa");
	cnt = new Container();
	scr = new JScrollPane(cnt);
	scr2 = new JScrollPane(tab);
	scr3 = new JScrollPane(jta);

 //set parameters
	tab.setCellSelectionEnabled(true);
	tab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	tab.setDragEnabled(false);
	tab.getTableHeader().setReorderingAllowed(false);
	for (i=0; i<5; i++) tab.getColumnModel().getColumn(i).setHeaderValue( head[i] );
	
	scr.setPreferredSize( new Dimension( (int) (SIZEX*0.8), (int) (SIZEY*0.8)) );
	jta.setEditable(false);
	jta.setBackground(clr);
	cnt.setLayout( new GridBagLayout() );
	
	for (i=0; i<2; ++i) mi[i].addActionListener(this);


 //put them togeather
	 c.insets.set(5,5,5,5); c.weightx=1; c.weighty=1; c.fill=GridBagConstraints.BOTH;
	cnt.add(scr2, c);

	 c.insets.set(0,5,5,5); c.weightx=0; c.weighty=0;
	 c.gridy=1; c.fill=GridBagConstraints.NONE; c.anchor = GridBagConstraints.WEST;
	cnt.add(scr3, c);

	//cnt.add(lab, c);
	
	add(scr);
	setJMenuBar(mbar);

	validate();
	setVisible(true);

 //menu...
	setPopupMenu();
	tab.addMouseListener(this);
	
} //constructor



private void setPopupMenu() {
	int i, j;
	
//1. initialize components
	pop = new JPopupMenu();
	pmenu = new JMenu[] { new JMenu("Πίνακας"),
							new JMenu("Αρχείο"), new JMenu("Περισσότερα") };
	
	pmi = new JMenuItem[][] {{ new JMenuItem("Υπολογισμός"), new JMenuItem("Αποκοπή"), new JMenuItem("Αντιγραφή"),
				new JMenuItem("Επικόληση"), new JMenuItem("Σύμπτιξη"), new JMenuItem("Διαγραφή")},
				{ new JMenuItem("Εισαγωγή Γραμμών") },
			{ new JMenuItem("Εισαγωγή"), new JMenuItem("Εξαγωγή"), new JMenuItem("Εξαγωγή όλων") },
				{ new JMenuItem("x <--> y") }};


 //2. Put the togeather
	pmenu[0].add(pmi[1][0]);
	
	pmenu[1].add(pmi[2][0]);
	pmenu[1].add(pmi[2][1]);
	pmenu[1].add(pmi[2][2]);
	
	pmenu[2].add(pmi[3][0]);
	
	pop.add(pmi[0][0]);
	
	pop.add(new JSeparator());
	for (i=0; i<3; ++i) pop.add( pmenu[i] );
	
	pop.add(new JSeparator());
	for (i=1; i<4; ++i) pop.add(pmi[0][i]);
	
	pop.add(new JSeparator());
	for (; i<pmi[0].length; ++i) pop.add(pmi[0][i]);

 //4. Add ToolTipTexts
	pmi[0][0].setToolTipText("Υπολογίζει τους συντελεστές Α και Β.");
	pmi[2][0].setToolTipText("Επιλέξτε αρχείο για εισαγωγή δεδομένων.");
	pmi[2][1].setToolTipText("Εξάγει τα δεδομένα σε αρχείο της επιλογής σας.");
	pmi[2][2].setToolTipText("Εξάγει δεδομένα και αποτελέσματα σε αρχείο της επιλογής σας.");
	pmi[3][0].setToolTipText("Αντιστρέφει τους ρόλους των x και y.");

 //5. Add Listeners
	for (i=pmi.length-1; i>=0; --i)
	for (j=pmi[i].length-1; j>=0; --j) pmi[i][j].addActionListener(this);

 //6. Put it!
	tab.add(pop);
} //setPopupMenu


private void addRows(int hm) { ((DefaultTableModel) tab.getModel()).setRowCount(tab.getRowCount()+hm); }

private void addRowsFromUser() {
	int i=0;
	String str, mes="Πόσες γραμμές θέλετε να προσθέσετε;";
	Scanner sc;
	
	for (;;) {
		str=JOptionPane.showInputDialog(this, mes, "Προσθήκη", JOptionPane.PLAIN_MESSAGE);
		if (str==null) return;
		
		sc = new Scanner(str);
		try { i = sc. nextInt();  if (i>0) break; }
			catch (Exception ex) {}
		mes = "Παρακαλώ δώστε θετικό ακέραιο!";
	} //for
	
	addRows(i);
}	//addRowsFromUser


private void calculate() {
	int N=0, rc = tab.getRowCount(), j;
	double x, y, sx=0, sx2=0, sxy=0, sy=0, sy2=0, par;

 //only selected
	if (tab.getSelectedRowCount()>1) 
		for (int i: tab.getSelectedRows()) {
		try {
	  //1A. read the data
			x = Double.parseDouble( tab.getValueAt(i,0).toString().replace(',','.') );
			y = Double.parseDouble( tab.getValueAt(i,1).toString().replace(',','.') );
		}//try
		catch (NullPointerException ex) { for(j=2; j<5; ++j) tab.setValueAt(null,i,j); continue; }
		catch (NumberFormatException ex2) { for(j=2; j<5; ++j) tab.setValueAt(null,i,j); continue; }

	 //2B. calculate and write sx sx2 etc.
		N++; sx+=x; sx2+=x*x; sxy+=x*y; sy+=y; sy2+=y*y;
		tab.setValueAt( String.valueOf(x*x), i,2 );
		tab.setValueAt( String.valueOf(x*y), i,3 );
		tab.setValueAt( String.valueOf(y*y), i, 4 );
	
		}//for

 //or all if not selected
	else		
		for (int i=0; i<rc; ++i) {
		try {
	  //1B. read the data
			x = Double.parseDouble( tab.getValueAt(i,0).toString().replace(',','.') );
			y = Double.parseDouble( tab.getValueAt(i,1).toString().replace(',','.') );
		}//try
		catch (NullPointerException ex) { for(j=2; j<5; ++j) tab.setValueAt(null,i,j); continue; }
		catch (NumberFormatException ex2) { for(j=2; j<5; ++j) tab.setValueAt(null,i,j); continue; }

	 //2B. calculate and write sx sx2 etc.
		N++; sx+=x; sx2+=x*x; sxy+=x*y; sy+=y; sy2+=y*y;
		tab.setValueAt( String.valueOf(x*x), i,2);
		tab.setValueAt( String.valueOf(x*y), i,3);
		tab.setValueAt( String.valueOf(y*y), i, 4);
	
		}//for
	
	
 //3. calculate par, a,b,da,db
	par = N*sx2 - sx*sx;
	a = (N*sxy - sx*sy)/ par;
	b = (sx2*sy - sx*sxy)/ par;
	r = (N*sxy - sx*sy) / Math.sqrt( (N*sx2-sx*sx)*(N*sy2 - sy*sy) );
		
	jta.setText( "N = "+N+LN+"Σx = "+sx+LN+ "Σy = "+sy+LN+"Σx^2 = "+sx2+LN+"Σxy = "+
					sxy+LN+"Σy^2 = "+sy2+LN+LN+"a = "+a+LN+"b = "+b+LN+"r = "+r);
}//calculate


private void exchangeXY() {
	int r,c;
	Object ob;

	for ( r=tab.getRowCount()-1; r>=0; --r ) {
	 //exchange
		ob = tab.getValueAt(r,0);
		tab.setValueAt( tab.getValueAt(r,1), r,0);
		tab.setValueAt( ob, r,1);
	 //reset
		for (c=2; c<5; c++) tab.setValueAt(null,r,c);
	}//for
}//exchangeXY

private File chooseFile() {

	JFileChooser fch = new JFileChooser();
	File f=null;
		
	int i= fch.showDialog(fch,"Διάλεξε");
	if (i==JFileChooser.APPROVE_OPTION) f = fch.getSelectedFile();
		
	return f;
}//chooseFile


private void readFromFile(File f) {
	try {
	
	int i=tab.getSelectedRow(), j, rc = tab.getRowCount();
	double x=0;
	Scanner sc = new Scanner(f).useLocale(java.util.Locale.US);
	
	for(;;) {
	try {
		
	 //1. write x
	 	for (;;)
		try {
		 x = Double.parseDouble( sc.next().replace(',','.') );
		 break;
		}//try
		catch (NumberFormatException nex) { continue; }
		tab.setValueAt( String.valueOf(x), i, 0);
		
		
	 //2. write y
	 	for (;;)
		try {
		 x = Double.parseDouble( sc.next().replace(',','.') );
		 break;
		}//try
		catch (NumberFormatException nex) { continue; }
		tab.setValueAt( String.valueOf(x), i, 1);
	
	 //3. clear other data
		for (j=2; j<5; ++j) tab.setValueAt(null, i, j);
		
	 //4. i++ and check if Table has space...
		i++;
		if (i==rc) { addRows(ADD); rc+=ADD; }
		 
	}	//try
	catch (java.util.NoSuchElementException ex) { break; }
	}	//for ;;
	
	}//try
	catch (Exception ex) {}
}	//readFromFile


private int writeToFile(File f, boolean all) {
 //1. show message and create File
	int ans=3, r=0;
	
	try {
	
	if (!f.exists()) f.createNewFile();
	else if (f.length()!=0) {
		String mes[] = new String[] {"Τίποτα", "Αντικατάσταση", "Προσθήκη"};
		ans = JOptionPane.showOptionDialog(this, "Το αρχείο υπάρχει ήδη. Τι θέλετε να γίνει;","Προσοχή",
		  JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, mes, mes[0]);
	} //else
	
 //2. check the answer
	if (ans<=0) return 1;
	FileWriter wr = new FileWriter(f, ans==2); //3 -> append
	Object ob;

 //3.write!
	for (int c: tab.getSelectedRows()) {
	
		for(r=0; r<2; ++r) {
		 ob = tab.getValueAt(c,r);
		 if (ob !=null) wr.write( (String) ob );
		 if (r==1) break;
		 wr.write("\t");
		} //for
		
		if (all) {
		for (r=2; r<5; ++r) {
		 wr.write("\t");
		 ob = tab.getValueAt(c,r);
		 if (ob !=null) wr.write( (String) ob );
		} //for
		} //if
		
		wr.write(LN);
	} //for
	
	wr.close();
	return 0;
	} //try
	catch (IOException ex) { return 1; }
}	//writeToFile



private void createInfoFrame(int wh) {
	
 //1. initialize
	char buf[] = new char[100];
	int i=0, j;
	String str="", mes = "Αρχείο δε βρέθηκε.";
	JFrame jf = new JFrame( (wh==0)? "Πληροφορίες" : "Βοήθεια");
	JTextArea jta2 = new JTextArea();
	JScrollPane scr1 = new JScrollPane(jta2);

 //2. set properties
	if (wh==0) jf.setSize( new Dimension(450,380) );
	else jf.setSize( new Dimension(800,430) );
	
	jta2.setBackground(clr);
	jta2.setEditable(false);
	jf.add(scr1);

 //3. read, and set text
	try {
	
	DataInputStream sc = new DataInputStream( new FileInputStream(fdata) );

	if (wh!=0)
	 while ( sc.readChar()!='\u0000') ;
	
	A: for (;;) {
		if (i==BLIM) { jta2.append( new String(buf) ); i=0; }

		buf[i] = sc.readChar();
		if (buf[i]== '\n') {
		 for (j=0; j<LN.length(); ++j) { buf[i++] = LN.charAt(j); }
		 continue A;
		}//if

		if ( buf[i]==0 ) { jta2.append( new String(buf,0,i) ); break; }
		i++;
	} //for
	
	} //try
	 catch (FileNotFoundException ex) { jta2.append(mes); }
	 catch (IOException ex) { jta2.append(mes); }
	
	
 //4. show frame
	jf.setVisible(true);
} //createInfoFrame


//================LISTENERS=====================

public void actionPerformed(ActionEvent e) {
	s = e.getSource();

	if (s==pmi[0][0]) { calculate(); }
	
	
	else if (s==pmi[0][1]) { //cut
		
		copied = new Object[tab.getSelectedColumnCount()][tab.getSelectedRowCount()];
		int i=0,j=0;
		for (int m: tab.getSelectedColumns()) {
		for (int n: tab.getSelectedRows()) {
			copied[j][i++] = tab.getValueAt(n,m);
			tab.setValueAt(null,n,m);
			}//for n
		j++; i=0;
		}//for m
	}	//pmi[0][1]
	
	
	else if (s==pmi[0][2]) { //copy
		copied = new Object[tab.getSelectedColumnCount()][tab.getSelectedRowCount()];
		int i=0,j=0;
		for (int m: tab.getSelectedColumns()) {
		for (int n: tab.getSelectedRows()) {
			copied[j][i++] = tab.getValueAt(n,m);
			}//for n
		j++; i=0;
		}//for m
	}	//pmi[0][2]
	
	
	else if (s==pmi[0][3]) { //paste
		if(copied==null) return;
		
		int sr = tab.getSelectedRow(), sc = tab.getSelectedColumn(), i=0, j;
		if (sc+copied.length>5) { return; }
		if (sr+copied[0].length>tab.getRowCount()) ((DefaultTableModel) tab.getModel()).setRowCount(sr+copied[0].length);
	
		for (;i<copied.length; ++i) {
		for (j=0; j<copied[0].length; ++j) tab.setValueAt(copied[i][j],sr+j,sc+i);
		}//for i
	}	//else if mi[5][3]
	
			
	else if (s==pmi[0][5]) {
		if (tab.getSelectedColumn() + tab.getSelectedColumnCount() >5 )
			{ return; }
			
			for (int m: tab.getSelectedColumns())
			for (int n: tab.getSelectedRows()) tab.setValueAt(null, n,m);
	}//pmi[0][5]
	
	
	else if (s==pmi[1][0]) addRowsFromUser();
	
	else if (s==pmi[2][0]) { //import
		File f = chooseFile();
		if (f!=null && f.exists()) readFromFile(f);
	} //pmi[2][0]
	
	
	else if (s==pmi[2][1] || s==pmi[2][2]) { //export
		File f = chooseFile();
		if (f!=null) writeToFile(f, s==pmi[2][2]);
	} //pmi[2][0]
	
	
	else if (s==pmi[3][0]) exchangeXY();
	
	
	else if (s==pmi[0][4]) { //compress
		int wr, br = tab.getSelectedRow(), lim = br+tab.getSelectedRowCount()-1,rr, cc;
		Object ob, ob2;
	
	
		wr=br;//begining of the reader
		
		while (tab.getValueAt(wr,0)!=null || tab.getValueAt(wr,0)!=null) 
		 if (++wr==lim)  return; //it means there are no null rows
	
		rr = wr+1;
		if (rr==lim-1) return; //it means that its only one null is at the end.
	
		while(rr<=lim) {

			ob = tab.getValueAt(rr,0); ob2 = tab.getValueAt(rr,1);

			while(ob==null && ob2==null) {
			 ob = tab.getValueAt(++rr,0); ob2 = tab.getValueAt(rr,1);
			 if (rr>lim) return;//it means that all nulls are at the end.
			}//while null
			
			tab.setValueAt(ob,wr,0); tab.setValueAt(ob2,wr,1); //write value and erase where it was before.
			for (cc=0; cc<5; ++cc) tab.setValueAt(null,rr,cc);
			
		while (tab.getValueAt(wr,0)!=null || tab.getValueAt(wr,0)!=null) 
		 if (++wr==lim)  return;
		 
		}//while rr<lim
	}//else if pmi[0][4]
	
	
	else if (s==mi[0]) createInfoFrame(0);
	else if (s==mi[1]) createInfoFrame(1);
	
} //actoinPerformed




public void mouseClicked(MouseEvent me) {
	if (me.getButton()==3) pop.show(tab,me.getX(),me.getY());
}//mouseClicked
public void mouseEntered(MouseEvent me) {}
public void mouseExited(MouseEvent me) {}
public void mousePressed(MouseEvent me) {}
public void mouseReleased(MouseEvent me) {}


public static void main(String[] arg) {
	try {
	
 //Set final variables
	ROWS = 50; //rows the array initially has
	SIZEX = 700; //size of the frame
	SIZEY = 700;
	LN = System.getProperty("line.separator"); //line
	BLIM = BSIZE-1-LN.length(); //Buffer limit. Used for createInfoFrame method
	fdata = new File("./data"); //file with "Help" and "About" text
 //start
	new MET();
	
	}//try
	catch (Exception ex) {ex.printStackTrace();}
}//main
} //class
