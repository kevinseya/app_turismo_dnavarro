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
        comments: true,
        user: {
          select: {
            id: true,
            name: true,
            email: true,
          },
        },
      },
    });
  }

  delete(id: number) {
    return this.prisma.post.update({
      where: { id },
      data: { isActive: false },
    });
  }
}
