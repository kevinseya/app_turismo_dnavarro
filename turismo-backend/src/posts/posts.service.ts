import { Injectable, Logger } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreatePostDto } from './dto/create-post.dto';

@Injectable()
export class PostsService {
  private readonly logger = new Logger(PostsService.name);
  constructor(private prisma: PrismaService) {}

  create(dto: CreatePostDto, userId: number) {
    this.logger.log(`Creando post en base de datos. userId: ${userId}, data: ${JSON.stringify(dto)}`);
    return this.prisma.post.create({
      data: {
        title: dto.title,
        description: dto.description,
        latitude: dto.latitude,
        longitude: dto.longitude,
        phone: dto.phone,
        images: dto.images ?? [],
        userId,
      },
    });
  }

  findAll() {
    return this.prisma.post.findMany({
      where: { isActive: true },
      include: {
        comments: {
          where: { isActive: true },
          include: { user: true },
          orderBy: { createdAt: 'desc' }
        },
        user: {
          select: {
            id: true,
            name: true,
            email: true,
          },
        },
        likes: true,
      },
      orderBy: { createdAt: 'desc' }
    });
  }

  findOne(id: number) {
    return this.prisma.post.findUnique({
      where: { id },
      include: {
        comments: {
          where: { isActive: true },
          include: { user: true },
          orderBy: { createdAt: 'desc' }
        },
        user: {
          select: {
            id: true,
            name: true,
            email: true,
          },
        },
        likes: true,
      },
    });
  }

  findByUser(userId: number) {
    return this.prisma.post.findMany({
      where: { userId, isActive: true },
      include: {
        comments: {
          where: { isActive: true },
          include: { user: true },
          orderBy: { createdAt: 'desc' }
        },
        user: {
          select: {
            id: true,
            name: true,
            email: true,
          },
        },
        likes: true,
      },
      orderBy: { createdAt: 'desc' }
    });
  }

  async delete(id: number, userId: number) {
    // Obtener el post
    const post = await this.prisma.post.findUnique({ where: { id } });
    if (!post) throw new Error('Post not found');
    // Permitir solo si es dueño o admin
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    const isAdmin = user?.role === 'ADMIN';
    if (post.userId !== userId && !isAdmin) {
      throw new Error('No autorizado para borrar este post');
    }
    return this.prisma.post.update({
      where: { id },
      data: { isActive: false },
    });
  }
}
