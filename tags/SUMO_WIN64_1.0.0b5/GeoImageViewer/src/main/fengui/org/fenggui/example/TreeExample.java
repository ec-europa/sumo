package org.fenggui.example;

import java.util.ArrayList;

import org.fenggui.Display;
import org.fenggui.ScrollContainer;
import org.fenggui.border.PlainBorder;
import org.fenggui.layout.StaticLayout;
import org.fenggui.render.Pixmap;
import org.fenggui.tree.*;
import org.fenggui.util.Color;

public class TreeExample implements IExample
{

	private MyNode root = null;
	
	public void buildGUI(Display display)
	{
		root = new MyNode("Italian Food");
	
		MyNode pasta = new MyNode("Pasta");
		MyNode antipasta = new MyNode("Antipasta");
		MyNode pizza = new MyNode("Pizza");
		MyNode caffe = new MyNode("Caffe");
		
		root.children.add(antipasta);
		root.children.add(pasta);
		root.children.add(pizza);
		root.children.add(caffe);
		
		pizza.children.add(new MyNode("Margherita"));
		pizza.children.add(new MyNode("Napoletana"));
		pizza.children.add(new MyNode("Giardiniera"));
		pizza.children.add(new MyNode("Capricciosa"));
		pizza.children.add(new MyNode("Prosciutto Cotto e Funghi"));
		pizza.children.add(new MyNode("Quattri Stagioni"));
		pizza.children.add(new MyNode("Tirolese"));
		pizza.children.add(new MyNode("Quattro Formaggi"));
		pizza.children.add(new MyNode("Siciliana"));
		pizza.children.add(new MyNode("Boscaiola"));
		pizza.children.add(new MyNode("Diavola"));
		pizza.children.add(new MyNode("Alla Salsiccia"));
		pizza.children.add(new MyNode("Calzone con Prosciutto e Funghi"));
		
		antipasta.children.add(new MyNode("Zuppa del Giorno"));
		antipasta.children.add(new MyNode("Bruschetta con Pomodoro"));
		antipasta.children.add(new MyNode("Mozzarella alla Caprese"));
		antipasta.children.add(new MyNode("Carpaccio con capperi e parmigiano"));
		antipasta.children.add(new MyNode("Calamaretti Fritti"));
		antipasta.children.add(new MyNode("Bresaola con Rucola"));
		antipasta.children.add(new MyNode("Melazzane alla Parmigiana"));
		antipasta.children.add(new MyNode("Carciofo Ripieno Freddo"));
		antipasta.children.add(new MyNode("Insalata Pane e Vino"));
		antipasta.children.add(new MyNode("Insalata di Spinaci"));
		antipasta.children.add(new MyNode("Insalata Misata"));
		antipasta.children.add(new MyNode("Antipasto della Casa"));
		
		caffe.children.add(new MyNode("Espresso"));
		caffe.children.add(new MyNode("Cappucino "));
		caffe.children.add(new MyNode("Caffe Latte"));
		caffe.children.add(new MyNode("Double Espresso"));
		caffe.children.add(new MyNode("Ruby Port"));
		caffe.children.add(new MyNode("Tawny Port"));
		
		ScrollContainer sc = new ScrollContainer();
		
		Tree<MyNode> tree = new Tree<MyNode>();
		sc.setInnerWidget(tree);
		
		sc.setSize(200, 300);
		StaticLayout.center(sc, display);
		display.addWidget(sc);
		sc.getAppearance().add(new PlainBorder(Color.BLACK));
		sc.layout();
		tree.setModel(new MyTreeModel());
		
	}

	public String getExampleName()
	{
		return "Tree Example";
	}

	public String getExampleDescription()
	{
		return "Shows a tree widget";
	}

	public class MyNode
	{
		public MyNode(String string)
		{
			this.text = string;
		}
		
		public ArrayList<MyNode> children = new ArrayList<MyNode>();
		public String text = null;
	}
	
	class MyTreeModel implements ITreeModel<MyNode>
	{

		public int getNumberOfChildren(MyNode node)
		{
			return node.children.size();
		}

		public Pixmap getPixmap(MyNode node)
		{
			return null;
		}

		public String getText(MyNode node)
		{
			return node.text;
		}

		public MyNode getRoot()
		{
			return root;
		}

		public MyNode getNode(MyNode parent, int index)
		{
			return parent.children.get(index);
		}


		
	}
	
}
