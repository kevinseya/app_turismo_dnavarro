import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { $Enums } from '@prisma/client';

@Injectable()
export class UsersService {
		async isFollowing(followerId: number, followingId: number): Promise<boolean> {
			const follow = await this.prisma.follow.findUnique({
				where: {
					followerId_followingId: {
						followerId,
						followingId,
					},
				},
			});
			return !!follow;
		}
	constructor(private prisma: PrismaService) {}

	findOne(id: number) {
		return this.prisma.user.findUnique({
			where: { id },
			select: {
				id: true,
				name: true,
				email: true,
				role: true,
				isActive: true,
				createdAt: true,
			},
		});
	}

	findAll() {
		return this.prisma.user.findMany({
			select: {
				id: true,
				name: true,
				email: true,
				role: true,
				isActive: true,
				createdAt: true,
			},
			orderBy: { createdAt: 'desc' },
		});
	}

	setActive(id: number, isActive: boolean) {
		return this.prisma.user.update({
			where: { id },
			data: { isActive },
		});
	}

	changeRole(id: number, role: string) {
		// Asegura que el valor sea del enum correcto
		const validRole = (role === 'ADMIN' || role === 'CLIENT') ? role : 'CLIENT';
		return this.prisma.user.update({
			where: { id },
			data: { role: validRole as $Enums.Role },
		});
	}
}
