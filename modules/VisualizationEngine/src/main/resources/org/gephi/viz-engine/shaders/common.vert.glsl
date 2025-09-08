#version 330
#define PI  3.14159265358
#define TAU 6.28318530718

#define _animationSlope(x) (1.-exp(-3.*x))

uniform float globalTime;
uniform float selectionTime;

float animationTime = globalTime-selectionTime;
float animationCurve = _animationSlope(animationTime); // Going from 0. to 1. https://graphtoy.com/?f1(x,t)=1.-exp(-5.*x)&v1=true&f2(x,t)=&v2=false&f3(x,t)=&v3=false&f4(x,t)=&v4=false&f5(x,t)=&v5=false&f6(x,t)=&v6=false&grid=1&coords=0,0,2.3741360268016223
