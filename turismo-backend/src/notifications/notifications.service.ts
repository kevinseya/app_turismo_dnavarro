import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { NotificationDto } from './dto/notification.dto';

@Injectable()
export class NotificationsService {
  constructor(private prisma: PrismaService) {}

  async sendNotification(dto: NotificationDto) {
    if (dto.userId) {
      // Notificación individual
      return this.prisma.notification.create({
        data: {
          title: dto.title,
          message: dto.message,
          userId: dto.userId,
        },
      });
    } else {
      // Notificación global: a todos los usuarios activos
      const users = await this.prisma.user.findMany({ where: { isActive: true } });
      return this.prisma.$transaction(
        users.map(user =>
          this.prisma.notification.create({
            data: {
              title: dto.title,
              message: dto.message,
              userId: user.id,
            },
          })
        )
      );
    }
  }

  async getUserNotifications(userId: number) {
    return this.prisma.notification.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
    });
  }
}
