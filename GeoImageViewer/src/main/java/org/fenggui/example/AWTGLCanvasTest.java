package org.fenggui.example;

import javax.media.opengl.GL2;
import javax.swing.JFrame;

import org.fenggui.render.lwjgl.AWTGLCanvasBinding;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 * Demonstrates how to use LWJGLs org.lwjgl.opengl.AWTGLCanvas instead of org.lwjgl.opengl.Display.
 *
 * @author Johannes, last edited by $Author: schabby $, $Date: 2006-10-05 03:37:07 +0200 (Thu, 05 Oct 2006) $
 * @version $Revision: 28 $
 */
public class AWTGLCanvasTest
{
	private JFrame frame = null;
	private MyAWTGLCanvas canvas = null;

	public AWTGLCanvasTest()
	{
		frame = new JFrame("Test Frame");
		frame.setSize(600, 600);

		try
		{
			canvas = new MyAWTGLCanvas();
			frame.getContentPane().add(canvas);
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args)
	{
		new AWTGLCanvasTest();
	}

	class MyAWTGLCanvas extends AWTGLCanvas
	{
		org.fenggui.Display fengGUIDisplay = null;

		public MyAWTGLCanvas() throws LWJGLException
		{
			super();

			fengGUIDisplay = new org.fenggui.Display(new AWTGLCanvasBinding(
					this));

			new org.fenggui.render.jogl.EventBinding(this, fengGUIDisplay);

			new Everything().buildGUI(fengGUIDisplay);

			Thread t = new Thread()
			{
				public void run()
				{
					while (true)
					{
						if (isVisible())
						{
							repaint();
						}
						Display.sync(60);
					}
				}
			};
			t.setDaemon(true);
			t.start();
		}

		 @Override
		protected void initGL()
		{
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GLU.gluPerspective(45, (float) 600 / (float) 600, 4, 1000);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glViewport(0, 0, 600, 600);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glShadeModel(GL2.GL_SMOOTH);
			//set clear color to ... ugly
			GL11.glClearColor(0.1f, 0.5f, 0.2f, 0.0f);
		}

		public void paintGL()
		{
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GLU.gluLookAt(10, 10, 10, 0, 0, 0, 0, 0, 1);
			GL11.glPushMatrix();
			GL11.glColor3f(1, 1, 1);
			// GL11.glTranslatef(320, 240, 0.0f);
			//GL11.glRotatef(angle, 0, 0, 1.0f);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3i(-1, -1, 0);
			GL11.glVertex3i(1, -1, 0);
			GL11.glVertex3i(1, 1, 0);
			GL11.glVertex3i(-1, 1, 0);
			GL11.glEnd();
			GL11.glPopMatrix();

			angle += 1;

			fengGUIDisplay.display();

			try
			{
				swapBuffers();
			}
			catch (Exception e)
			{
			}

		}

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private float angle = 0;
	}

}
