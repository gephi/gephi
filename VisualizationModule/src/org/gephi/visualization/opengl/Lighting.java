/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gephi.visualization.opengl;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

/**
 * Create the lighting attributes and manage which lights are enabled depending of the presets.
 *
 * @author Mathieu Jacomy
 */
public class Lighting {
	///// Attributs
	// Types de projecteurs
	public static final int TYPE_AMBIANT       = 1;
	public static final int TYPE_GAUCHE_JAUNE  = 2;
	public static final int TYPE_HAUT_BLEU     = 3;
	public static final int TYPE_BAS_ROUGE     = 4;
	public static final int TYPE_SPOT_BLAFARD  = 5;
	public static final int TYPE_NATUREL       = 6;
	public static final int TYPE_LATERAL_BLANC = 7;
	public static final int TYPE_LATERAL_MULTI = 8;
	// Couleurs
	private static float blanc[]       = {(float) 255/255, (float) 255/255, (float) 255/255, 0.0f};
	private static float blancCasse[]  = {(float) 213/255, (float) 208/255, (float) 188/255, 0.0f};
	private static float medium[]      = {(float) 143/255, (float) 141/255, (float) 132/255, 0.0f};
	private static float obscur[]      = {(float)  76/255, (float)  83/255, (float)  83/255, 0.0f};
	private static float noirCasse[]   = {(float)  39/255, (float)  25/255, (float)  99/255, 0.0f};
	private static float noir[]        = {(float)   0/255, (float)   0/255, (float)   0/255, 0.0f};
	private static float rougeFaible[] = {(float) 155/255, (float) 071/255, (float) 052/255, 0.0f};
	private static float rouge[]       = {(float) 223/255, (float)  80/255, (float)  80/255, 0.0f};
	private static float rougeFort[]   = {(float) 255/255, (float)  35/255, (float)  61/255, 0.0f};
	private static float jauneFaible[] = {(float) 103/255, (float) 100/255, (float)  46/255, 0.0f};
	private static float jaune[]       = {(float) 192/255, (float) 187/255, (float)  55/255, 0.0f};
	private static float jauneFort[]   = {(float) 244/255, (float) 228/255, (float)  56/255, 0.0f};
	private static float bleuFaible[]  = {(float)  80/255, (float) 113/255, (float) 138/255, 0.0f};
	private static float bleu[]        = {(float) 114/255, (float) 126/255, (float) 203/255, 0.0f};
	private static float bleuFort[]    = {(float)  95/255, (float)  94/255, (float) 240/255, 0.0f};
	private static float vert[]        = {(float) 120/255, (float) 146/255, (float)  97/255, 0.0f};
	private static float orange[]      = {(float) 215/255, (float) 183/255, (float)  86/255, 0.0f};
	private static float violet[]      = {(float) 153/255, (float)  90/255, (float) 162/255, 0.0f};
	// Directions
	private static float biaisDessus[]  = { -1.0f,  2.0f,   -1.0f,  0.0f };
	private static float haut[]         = {  0.0f,  2.0f,   0.0f,  0.0f };
	private static float gauche[]       = { 2.0f,  0.0f,   1.0f,  0.0f };
	private static float hautGauche[]   = { 0.5f,  1.0f,   2.0f,  0.0f };
	private static float basDroite[]    = {  -1.0f, -1.2f,  -0.5f,  0.0f };
	private static float quasiDessus[]  = {  0.5f, -0.5f,  -3.0f,  0.0f };
	private static float droit[]        = {  0.0f,  0.0f,  -1.0f,  0.0f };
	// Positions
	private static float centre[]       = {  0.0f,  0.0f, 3.0f,  1.0f };
	private static float quasiCentre[]  = { -2.0f,  2.0f, 9.0f,  1.0f };
	// Buffers
	private static FloatBuffer ltDirection;
	private static FloatBuffer ltDiffuse;
	private static FloatBuffer ltSpecular;
	private static FloatBuffer ltAmbient;
	private static FloatBuffer ltPosition;
	// Etats des lampes
	private static boolean state0 = true;
	private static boolean state1 = false;
	private static boolean state2 = false;
	private static boolean state3 = false;
	private static boolean state4 = false;
	private static boolean state5 = false;
	private static boolean state6 = false;
	private static boolean state7 = false;
	///// Constructeurs
	Lighting() {
	}

	///// Méthodes
	// Setting lights
	public static void setSource(int lightNumber, int lightType, GL gl){
		int lh = 0;
		switch(lightNumber){
		case(0) :
			lh=GL.GL_LIGHT0;
		break;

		case(1) :
			lh=GL.GL_LIGHT1;
		break;

		case(2) :
			lh=GL.GL_LIGHT2;
		break;

		case(3) :
			lh=GL.GL_LIGHT3;
		break;

		case(4) :
			lh=GL.GL_LIGHT4;
		break;

		case(5) :
			lh=GL.GL_LIGHT5;
		break;

		case(6) :
			lh=GL.GL_LIGHT6;
		break;

		case(7) :
			lh=GL.GL_LIGHT7;
		break;
		}


		switch(lightType){

		case(TYPE_AMBIANT) :
			gl.glLightfv( lh, GL.GL_DIFFUSE, noirCasse,0);   // color of the direct illumination
		gl.glLightfv( lh, GL.GL_SPECULAR,noir,0);  // color of the highlight
		gl.glLightfv( lh, GL.GL_AMBIENT,obscur,0);   // color of the reflected light
		break;

		case(TYPE_NATUREL) :
			gl.glLightfv( lh, GL.GL_DIFFUSE,medium,0);   // color of the direct illumination
		gl.glLightfv( lh, GL.GL_SPECULAR,jaune,0);  // color of the highlight
		gl.glLightfv( lh, GL.GL_AMBIENT,noirCasse,0);   // color of the reflected light
		gl.glLightfv( lh, GL.GL_POSITION,hautGauche,0);
		break;

		case(TYPE_GAUCHE_JAUNE) :
			gl.glLightfv( lh, GL.GL_DIFFUSE, jauneFaible,0);   // color of the direct illumination
		gl.glLightfv( lh, GL.GL_SPECULAR,  jauneFort,0);  // color of the highlight
		gl.glLightfv( lh, GL.GL_AMBIENT,    noir,0);   // color of the reflected light
		gl.glLightfv( lh, GL.GL_POSITION,   gauche,0);
		break;

		case(TYPE_HAUT_BLEU) :
			gl.glLightfv( lh, GL.GL_DIFFUSE, bleuFaible,0);   // color of the direct illumination
		gl.glLightfv( lh, GL.GL_SPECULAR, bleuFort,0);  // color of the highlight
		gl.glLightfv( lh, GL.GL_AMBIENT, noir,0);   // color of the reflected light
		gl.glLightfv( lh, GL.GL_POSITION, haut,0);
		break;

		case(TYPE_BAS_ROUGE) :
			gl.glLightfv( lh, GL.GL_DIFFUSE,rougeFaible,0);   // color of the direct illumination
		gl.glLightfv( lh, GL.GL_SPECULAR, rouge,0);  // color of the highlight
		gl.glLightfv( lh, GL.GL_AMBIENT, noir,0);   // color of the reflected light
		gl.glLightfv( lh, GL.GL_POSITION, basDroite,0);
		break;

		case(TYPE_SPOT_BLAFARD) :
			gl.glLightfv( lh, GL.GL_DIFFUSE,jauneFaible,0);   // color of the direct illumination
		//gl.glLightfv( lh, GL.GL_SPECULAR,obscur,0);  // color of the highlight
		gl.glLightfv( lh, GL.GL_AMBIENT,bleuFaible,0);   // color of the reflected light
		gl.glLightfv( lh, GL.GL_POSITION,centre,0);
		gl.glLightf(lh, GL.GL_SPOT_CUTOFF,8f);         // width of the beam
		gl.glLightf(lh, GL.GL_SPOT_EXPONENT,20f);
		gl.glLightfv( lh, GL.GL_SPOT_DIRECTION,droit,0); // which way it points
		break;

		case(TYPE_LATERAL_BLANC) :
			gl.glLightfv( lh, GL.GL_DIFFUSE,blanc,0);   // color of the direct illumination
		gl.glLightfv( lh, GL.GL_SPECULAR,blanc,0);  // color of the highlight
		gl.glLightfv( lh, GL.GL_AMBIENT,noir,0);   // color of the reflected light
		gl.glLightfv( lh, GL.GL_POSITION,gauche,0);
		break;

		case(TYPE_LATERAL_MULTI) :
			gl.glLightfv( lh, GL.GL_DIFFUSE,rouge,0);   // color of the direct illumination
		gl.glLightfv( lh, GL.GL_SPECULAR,vert,0);  // color of the highlight
		gl.glLightfv( lh, GL.GL_AMBIENT,noirCasse,0);   // color of the reflected light
		gl.glLightfv( lh, GL.GL_POSITION,gauche,0);
		break;

		default :

			break;
		}
	}
	// Switch on, off
	public static void switchOn(int lightNumber,GL gl){
		switch(lightNumber){
		case(0) :
			gl.glEnable(GL.GL_LIGHT0);
		state0 = true;
		break;

		case(1) :
			gl.glEnable(GL.GL_LIGHT1);
		state1 = true;
		break;

		case(2) :
			gl.glEnable(GL.GL_LIGHT2);
		state2 = true;
		break;

		case(3) :
			gl.glEnable(GL.GL_LIGHT3);
		state3 = true;
		break;

		case(4) :
			gl.glEnable(GL.GL_LIGHT4);
		state4 = true;
		break;

		case(5) :
			gl.glEnable(GL.GL_LIGHT5);
		state5 = true;
		break;

		case(6) :
			gl.glEnable(GL.GL_LIGHT6);
		state6 = true;
		break;

		case(7) :
			gl.glEnable(GL.GL_LIGHT7);
		state7 = true;
		break;
		}
	}
	public static void switchOff(int lightNumber, GL gl){
		switch(lightNumber){
		case(0) :
			gl.glDisable(GL.GL_LIGHT0);
		state0 = false;
		break;

		case(1) :
			gl.glDisable(GL.GL_LIGHT1);
		state1 = false;
		break;

		case(2) :
			gl.glDisable(GL.GL_LIGHT2);
		state2 = false;
		break;

		case(3) :
			gl.glDisable(GL.GL_LIGHT3);
		state3 = false;
		break;

		case(4) :
			gl.glDisable(GL.GL_LIGHT4);
		state4 = false;
		break;

		case(5) :
			gl.glDisable(GL.GL_LIGHT5);
		state5 = false;
		break;

		case(6) :
			gl.glDisable(GL.GL_LIGHT6);
		state6 = false;
		break;

		case(7) :
			gl.glDisable(GL.GL_LIGHT7);
		state7 = false;
		break;
		}
	}
	public static void switchState(int lightNumber, GL gl){
		switch(lightNumber){
		case(0) :
			if(state0){
				switchOff(0,gl);
			} else {
				switchOn(0,gl);
			}
		break;

		case(1) :
			if(state1){
				switchOff(1,gl);
			} else {
				switchOn(1,gl);
			}
		break;

		case(2) :
			if(state2){
				switchOff(2,gl);
			} else {
				switchOn(2,gl);
			}
		break;

		case(3) :
			if(state3){
				switchOff(3,gl);
			} else {
				switchOn(3,gl);
			}
		break;

		case(4) :
			if(state4){
				switchOff(4,gl);
			} else {
				switchOn(4,gl);
			}
		break;

		case(5) :
			if(state5){
				switchOff(5,gl);
			} else {
				switchOn(5,gl);
			}
		break;

		case(6) :
			if(state6){
				switchOff(6,gl);
			} else {
				switchOn(6,gl);
			}
		break;

		case(7) :
			if(state7){
				switchOff(7,gl);
			} else {
				switchOn(7,gl);
			}
		break;
		}
	}
	public static void switchAll(GL gl, boolean li0, boolean li1, boolean li2, boolean li3, boolean li4, boolean li5, boolean li6, boolean li7){
		if(li0){
			gl.glEnable(GL.GL_LIGHT0);
		} else {
			gl.glDisable(GL.GL_LIGHT0);
		}
		if(li1){
			gl.glEnable(GL.GL_LIGHT1);
		} else {
			gl.glDisable(GL.GL_LIGHT1);
		}
		if(li2){
			gl.glEnable(GL.GL_LIGHT2);
		} else {
			gl.glDisable(GL.GL_LIGHT2);
		}
		if(li3){
			gl.glEnable(GL.GL_LIGHT3);
		} else {
			gl.glDisable(GL.GL_LIGHT3);
		}
		if(li4){
			gl.glEnable(GL.GL_LIGHT4);
		} else {
			gl.glDisable(GL.GL_LIGHT4);
		}
		if(li5){
			gl.glEnable(GL.GL_LIGHT5);
		} else {
			gl.glDisable(GL.GL_LIGHT5);
		}
		if(li6){
			gl.glEnable(GL.GL_LIGHT6);
		} else {
			gl.glDisable(GL.GL_LIGHT6);
		}
		if(li7){
			gl.glEnable(GL.GL_LIGHT7);
		} else {
			gl.glDisable(GL.GL_LIGHT7);
		}

	}
}

