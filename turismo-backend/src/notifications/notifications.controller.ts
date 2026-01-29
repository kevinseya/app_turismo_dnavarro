import { Controller, Post, Body, UseGuards, Req, Get } from '@nestjs/common';
import { NotificationsService } from './notifications.service';
import { NotificationDto } from './dto/notification.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { RolesGuard } from '../auth/roles.guard';
import { Roles } from '../auth/roles.decorator';

@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('notifications')
export class NotificationsController {
  constructor(private readonly notificationsService: NotificationsService) {}

  // Solo ADMIN puede enviar notificaciones
  @Roles('ADMIN')
  @Post('send')
  send(@Body() dto: NotificationDto) {
    return this.notificationsService.sendNotification(dto);
  }

  // Cualquier usuario puede ver sus notificaciones
  @UseGuards(JwtAuthGuard)
  @Get('my')
  getMyNotifications(@Req() req) {
    return this.notificationsService.getUserNotifications(req.user.sub);
  }
}
