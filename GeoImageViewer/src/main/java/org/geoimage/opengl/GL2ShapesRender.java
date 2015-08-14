package org.geoimage.opengl;

import java.awt.Color;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class GL2ShapesRender {

	public static void renderPolygons(OpenGLContext context,float zoomWidth,float zoomHeight,List<Geometry>geometries,float size,Color color){
		GL2 gl = context.getGL().getGL2();
		float[] c = color.brighter().getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);
		gl.glPointSize(size);
        gl.glBegin(GL.GL_POINTS);
        for (Geometry temp : geometries) {
            for (Coordinate point : temp.getCoordinates()) {
                gl.glVertex2d((point.x - context.getX()) / zoomWidth, 1 - (point.y - context.getY()) / zoomHeight);
            }
        }
        gl.glEnd();
        gl.glFlush();
	}
	
	
	public static void renderPolygon(OpenGLContext context,float zoomWidth,float zoomHeight,Coordinate[] coordinates,float size,Color color){
		GL2 gl = context.getGL().getGL2();
		float[] c = color.brighter().getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);
		gl.glPointSize(size);
        gl.glBegin(GL.GL_LINE_STRIP);
        for (Coordinate point : coordinates) {
            gl.glVertex2d((point.x - context.getX()) / zoomWidth, 1 - (point.y - context.getY()) / zoomHeight);
        }
        //Coordinate point = temp.getCoordinates()[0];
        //gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);

        gl.glEnd();
        gl.glFlush();
	}
	
	
	public static void renderPoint(OpenGLContext context,float zoomWidth,float zoomHeight,Coordinate point,float size,Color color){
		GL2 gl = context.getGL().getGL2();
        float[] c = color.brighter().getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);
        gl.glPointSize(size);
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex2d((point.x - context.getX()) / zoomWidth, 1 - (point.y - context.getY()) / zoomHeight);
        gl.glEnd();
        gl.glFlush();
	}
	
	
	
	public static void renderSquare(OpenGLContext context,float zoomWidth,float zoomHeight,List<Geometry>geometries,Geometry selectedGeometry,float renderWidth,Color color){
		GL2 gl = context.getGL().getGL2();
        float[] c = color.brighter().getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);

		for (Geometry temp : geometries) {
            gl.glLineWidth(temp == selectedGeometry ? renderWidth * 3 : renderWidth);
            Coordinate point = new Coordinate(temp.getCoordinate());
            point.x = (point.x - context.getX()) / zoomWidth;
            point.y = 1 - (point.y - context.getY()) / zoomHeight;
            double rectwidth = 0.01;
            gl.glBegin(GL.GL_LINE_STRIP);
            gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
            gl.glVertex2d(point.x - rectwidth, point.y + rectwidth);
            gl.glVertex2d(point.x + rectwidth, point.y + rectwidth);
            gl.glVertex2d(point.x + rectwidth, point.y - rectwidth);
            gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
            gl.glEnd();
            gl.glFlush();
        }
	}	
	public static void renderCross(OpenGLContext context,float zoomWidth,float zoomHeight,List<Geometry>geometries,Geometry selectedGeometry,float renderWidth,Color color){
		GL2 gl = context.getGL().getGL2();
        float[] c = color.brighter().getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);
		for (Geometry temp : geometries) {
	        gl.glLineWidth(temp == selectedGeometry ? renderWidth * 2 : renderWidth);
	        Coordinate point = new Coordinate(temp.getCoordinate());
	        point.x = (point.x - context.getX()) / zoomWidth;
	        point.y = 1 - (point.y - context.getY()) / zoomHeight;
	        double rectwidth = 0.01;
	        gl.glBegin(GL.GL_LINE_STRIP);
	        gl.glVertex2d(point.x - rectwidth, point.y);
	        gl.glVertex2d(point.x + rectwidth, point.y);
	        gl.glEnd();
	        gl.glBegin(GL.GL_LINE_STRIP);
	        gl.glVertex2d(point.x, point.y - rectwidth);
	        gl.glVertex2d(point.x, point.y + rectwidth);
	        gl.glEnd();
	        gl.glFlush();
	    }
	}
	public static void renderTriangle(OpenGLContext context,float zoomWidth,float zoomHeight,List<Geometry>geometries,Geometry selectedGeometry,float renderWidth,Color color){
		GL2 gl = context.getGL().getGL2();
        float[] c = color.brighter().getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);
		for (Geometry temp : geometries) {
	        gl.glLineWidth(temp == selectedGeometry ? renderWidth * 2 : renderWidth);
	        Coordinate point = new Coordinate(temp.getCoordinate());
	        point.x = (point.x - context.getX()) / zoomWidth;
	        point.y = 1 - (point.y - context.getY()) / zoomHeight;
	        double rectwidth = 0.01;
	        gl.glBegin(GL.GL_LINE_STRIP);
	        gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
	        gl.glVertex2d(point.x, point.y + rectwidth);
	        gl.glVertex2d(point.x + rectwidth, point.y - rectwidth);
	        gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
	        gl.glEnd();
	        gl.glFlush();
	    }
	}	
	
	public static void renderCircle(OpenGLContext context,float zoomWidth,float zoomHeight,List<Geometry>geometries,float size,Color color){
		GL2 gl = context.getGL().getGL2();
		gl.glBegin(GL.GL_POINTS);
    	gl.glLineWidth(size);
    	for (int ii=0;ii<geometries.size();ii++) {
    	   Geometry temp =geometries.get(ii);
           Coordinate point = temp.getCoordinate();
           double dx=(point.x - context.getX()) / zoomWidth;
           double dy=1 - (point.y - context.getY()) / zoomHeight;
    	   for (int i=0; i < 360; i++){
    		   //double angle = 2 * Math.PI * i / 360;
    		   double xx = dx+Math.sin(i)*0.005;
    		   double yy = dy+Math.cos(i)*0.005;
    		   
    		   gl.glVertex2d(xx,yy);
    	   }
        } 
	    gl.glEnd();
        gl.glFlush();
	}
	
	/**
     * 
     * @param gl
     * @param cs
     * @param width
     * @param height
     * @param x
     * @param y
     */
    public static void drawPoly(OpenGLContext context,Coordinate[] cs,float width,float height,int x,int y,float rwidth,Color color){
		GL2 gl = context.getGL().getGL2();
		float[] c = color.getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);
    	gl.glLineWidth(rwidth);
        gl.glBegin(GL.GL_LINE_STRIP);
        for (int p = 0; p < cs.length; p++) {
        	double vx=(cs[p].x - x) / width;
        	double vy=1 - (cs[p].y - y) / height;
            gl.glVertex2d(vx,vy);
        }
       
        //close polygon
        Coordinate point = cs[0];
        gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
        gl.glEnd();
        gl.glFlush();
    }
}
