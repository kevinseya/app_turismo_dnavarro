import { Controller, Get, UseGuards } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { RolesGuard } from '../auth/roles.guard';
import { Roles } from '../auth/roles.decorator';

@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('admin')
export class AdminController {
  constructor(private prisma: PrismaService) {}

  // Dashboard de métricas
  @Roles('ADMIN')
  @Get('dashboard')
  async dashboard() {
    const [users, posts, comments, likes] = await Promise.all([
      this.prisma.user.count(),
      this.prisma.post.count(),
      this.prisma.comment.count(),
      this.prisma.like.count(),
    ]);
    const topPosts = await this.prisma.post.findMany({
      orderBy: { likesCount: 'desc' },
      take: 5,
      select: { id: true, title: true, likesCount: true, commentsCount: true },
    });
    return {
      totalUsers: users,
      totalPosts: posts,
      totalComments: comments,
      totalLikes: likes,
      topPosts,
    };
  }
}
