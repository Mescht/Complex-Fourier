import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;
public class Main extends PApplet {

	public static void main(String[] args) {
		PApplet.main("Main");
	}
	
	//list of complex coefficients of the fourier series ordered 0, 1, -1, 2, -2 ... n, -n
	Complex[] coeffs;
	
	//scale factor for drawing
	float scale = (float) 1;
	
	float time = 0;
	
	//list of previous drawn point (for trail effect)
	ArrayList<double[]> particles = new ArrayList<double[]>();

	//list of complex points which make up the function
	Complex[] points;
	
	//depth of the fourier series
	int renderdetail = 500;
	
	Complex tip = new Complex(0, 0);
	boolean follow = false;
			
	public void settings() {
		size(1024, 1024);
	}
	
	public void setup() {
		
		//load list of points
//		readFile("fourier_portrait.txt");
		readFile("bird.txt");
		//calculate all the complex coefficients
		coeffs = calculateCoeffs(renderdetail);
		//set 0th coefficient to 0 to center the drawing
		coeffs[0].set(0, 0);
		
		frameRate(1000);
	}
	
	public void draw() {
		background(30);
		noFill();
		
		//translate to center of canvas
		translate(width / 2, height / 2);
		
		//translate to follow the tip of the animation
		if(follow) {			
			translate((float)-tip.re * scale, (float)-tip.im * scale);
		}
		
		Complex foot = new Complex(0, 0);
		Complex head = new Complex(0, 0);
		
		strokeWeight(1);
		
		for(int i = 0; i < renderdetail; i++) {
			
			//0 1 -1 2 -2... n -n
			int n = (int)(Math.ceil((double)i / 2) * ((double)(i % 2) - 0.5) * 2); 
			
			//nth complex exponent of the expression [n*2PI*i*t]
			Complex exponent = new Complex(0, n * Math.PI * 2 * time);
			
			head = foot.plus(coeffs[i].times(exponent.exp()));
			
			stroke(255, 255, 255, 255);
			line((float)foot.re * scale, (float)foot.im * scale, (float)head.re * scale, (float)head.im * scale);
			stroke(255, 255, 255, 100);
			ellipse((float)foot.re * scale, (float)foot.im * scale, (float)coeffs[i].abs() * scale * 2, (float)coeffs[i].abs() * scale * 2);
			foot = head;
		}
		
		tip = head.clone();
		
		particles.add(new double[] {head.re, head.im});
		
		strokeWeight(3);
		int c = 0;
		/*
		for(int i = particles.size() - 1; i >= 0; i--) {
			stroke(0,255,255,i / 4);
			point((float)particles.get(i)[0] * scale,(float)particles.get(i)[1] * scale);
			c++;
			if(c > 2000) {
				particles.remove(i);
			}
		}
		*/
		for(int i = particles.size() - 2; i >= 0; i--) {
			stroke(0,255,255,i / 4);
			line((float)particles.get(i)[0] * scale, (float)particles.get(i)[1] * scale, (float)particles.get(i + 1)[0] * scale,(float)particles.get(i + 1)[1] * scale);
			c++;
			if(c > 2000) {
				particles.remove(i);
			}
		}
		
		time += 0.0005;
		
	}

	public void mouseWheel(MouseEvent event) {
		scale = (float)(scale * Math.pow(2, -event.getCount()));
	}
	
	public Complex[] calculateCoeffs(int detail) {
		
		Complex[] ret = new Complex[detail * 2 - 1];
		
		Complex dt = new Complex(1.0 / points.length, 0);
		
		for(int i = 0; i < detail * 2 - 1; i++) {
			
			Complex sum = new Complex(0, 0);
			
			int n = (int)(Math.ceil((double)i / 2) * ((double)(i % 2) - 0.5) * 2);
			
			for(int j = 0 ; j < points.length; j++) {
				double t = (double)j / points.length;
				Complex exponent = new Complex(0, -2*Math.PI*n*t);
				Complex f = points[j].times(exponent.exp());

				sum = sum.plus(f.times(dt));
			}
			
			
			ret[i] = new Complex(sum.re, sum.im);
		}
		
		return ret;
	}
	
	public void keyPressed() {
		switch(key) {
		case 'f':
			follow = !follow;
		}
	}
	
	private void readFile(String s) {
		
		ArrayList<Complex> p = new ArrayList<Complex>();
		
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(s));
			
			while((line = br.readLine()) != null) {
				int sep = line.indexOf(',');
				double re = Double.parseDouble(line.substring(0, sep));
				double im = Double.parseDouble(line.substring(sep + 1));
				p.add(new Complex(re, im));
			}
		} catch(IOException e) {
			
		}
	
		points = p.toArray(new Complex[0]);
	}
}
