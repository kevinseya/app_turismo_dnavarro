import { Injectable, BadRequestException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class FollowService {
  constructor(private prisma: PrismaService) {}

  async follow(userId: number, targetId: number) {
    if (userId === targetId) {
      throw new BadRequestException('You cannot follow yourself');
    }

    return this.prisma.follow.create({
      data: {
        followerId: userId,
        followingId: targetId,
      },
    });
  }

  async unfollow(userId: number, targetId: number) {
    return this.prisma.follow.delete({
      where: {
        followerId_followingId: {
          followerId: userId,
          followingId: targetId,
        },
      },
    });
  }

  async followersCount(userId: number) {
    return this.prisma.follow.count({
      where: { followingId: userId },
    });
  }

  async followingCount(userId: number) {
    return this.prisma.follow.count({
      where: { followerId: userId },
    });
  }
}
