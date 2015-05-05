package net.eekysam.creeps.grow.sim;

import net.eekysam.creeps.grow.CreepSpec;

import org.lwjgl.input.Keyboard;

public class PlayerCreep extends Creep
{
	int red = 0;
	int green = 0;
	
	boolean rdown = false;
	boolean gdown = false;
	
	public PlayerCreep(CreepSpec spec)
	{
		super(spec);
	}
	
	@Override
	public void tick(EnumTickPass pass)
	{
		super.tick(pass);
		if (pass == EnumTickPass.LAST)
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_Q))
			{
				this.rot += this.spec.rotSpeed * this.speed;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_E))
			{
				this.rot -= this.spec.rotSpeed * this.speed;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_W))
			{
				this.velx += this.cos * this.spec.accel * this.speed;
				this.vely += this.sin * this.spec.accel * this.speed;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_S))
			{
				this.velx += this.cos * -this.spec.accel * this.speed;
				this.vely += this.sin * -this.spec.accel * this.speed;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_A))
			{
				this.velx += -this.sin * this.spec.accelSide * this.speed;
				this.vely += this.cos * this.spec.accelSide * this.speed;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_D))
			{
				this.velx += -this.sin * -this.spec.accelSide * this.speed;
				this.vely += this.cos * -this.spec.accelSide * this.speed;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_R))
			{
				if (!this.rdown)
				{
					this.red += 32;
					this.rdown = true;
				}
			}
			else
			{
				this.rdown = false;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_G))
			{
				if (!this.gdown)
				{
					this.green += 32;
					this.gdown = true;
				}
			}
			else
			{
				this.gdown = false;
			}
			
			if (this.red == 256)
			{
				this.red = 255;
			}
			else if (this.red > 256)
			{
				this.red = 0;
			}
			if (this.green == 256)
			{
				this.green = 255;
			}
			else if (this.green > 256)
			{
				this.green = 0;
			}
			
			this.myColor = this.spec.baseColor;
			this.myColor |= (this.red & 0xFF) << 16;
			this.myColor |= (this.green & 0xFF) << 8;
		}
	}
}
