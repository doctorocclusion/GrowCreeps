package net.eekysam.creeps.grow.sim;

import org.lwjgl.input.Keyboard;

public class PlayerCreep extends Creep
{
	int red = 0;
	int blue = 0;
	
	@Override
	public void tick(double rate, EnumTickPass pass)
	{
		super.tick(rate, pass);
		if (pass == EnumTickPass.APPLY)
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_Q))
			{
				this.rot += rotSpeed * rate;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_E))
			{
				this.rot -= rotSpeed * rate;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_W))
			{
				this.velx += this.cos * acc * rate;
				this.vely += this.sin * acc * rate;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_S))
			{
				this.velx += this.cos * -acc * rate;
				this.vely += this.sin * -acc * rate;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_A))
			{
				this.velx += this.sin * -sideAcc * rate;
				this.vely += this.cos * -sideAcc * rate;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_D))
			{
				this.velx += this.sin * sideAcc * rate;
				this.vely += this.cos * sideAcc * rate;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_R))
			{
				this.red += 32;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_B))
			{
				this.blue += 32;
			}
			
			this.myColor = defColor;
			this.myColor |= (this.red & 0xFF) << 0;
			this.myColor |= (this.blue & 0xFF) << 16;
		}
	}
}
