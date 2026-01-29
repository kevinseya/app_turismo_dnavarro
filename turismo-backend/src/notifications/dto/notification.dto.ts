import { IsString, IsOptional } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class NotificationDto {
  @ApiProperty({ example: '¡Nuevo post destacado!' })
  @IsString()
  title: string;

  @ApiProperty({ example: 'Visita la cascada azul, recomendada por la comunidad.' })
  @IsString()
  message: string;

  @ApiPropertyOptional({ example: 1, description: 'ID del usuario receptor. Si no se envía, es global.' })
  @IsOptional()
  userId?: number;
}
