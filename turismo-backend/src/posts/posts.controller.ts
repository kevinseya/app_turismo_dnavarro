import {
  Controller,
  Post,
  Get,
  Delete,
  Body,
  Param,
  Req,
  UseGuards,
  UploadedFiles,
  UseInterceptors,
  Logger,
} from '@nestjs/common';
import { FilesInterceptor } from '@nestjs/platform-express';
import { ApiConsumes, ApiBody, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { diskStorage } from 'multer';
import { extname } from 'path';
import { PostsService } from './posts.service';
import { CreatePostDto } from './dto/create-post.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { RolesGuard } from '../auth/roles.guard';
import { Roles } from '../auth/roles.decorator';

@Controller('posts')
export class PostsController {
  private readonly logger = new Logger(PostsController.name);
  constructor(private postsService: PostsService) {}


  // 🔐 todos crean
  @UseGuards(JwtAuthGuard)
  @Post('upload')
  @ApiOperation({ summary: 'Subir imágenes para posts (solo ADMIN)' })
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        images: {
          type: 'array',
          items: {
            type: 'string',
            format: 'binary',
          },
        },
      },
    },
  })
  @ApiResponse({ status: 201, description: 'Rutas de las imágenes subidas', type: [String] })
  @UseInterceptors(FilesInterceptor('images', 5, {
    storage: diskStorage({
      destination: './uploads',
      filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
        cb(null, uniqueSuffix + extname(file.originalname));
      },
    }),
    fileFilter: (req, file, cb) => {
      if (!file.mimetype.match(/^image\//)) {
        return cb(new Error('Only image files are allowed!'), false);
      }
      cb(null, true);
    },
  }))
  uploadImages(@UploadedFiles() files: Express.Multer.File[]) {
    // Devuelve las rutas relativas para guardar en el post
    return files.map(file => file.path.replace('\\', '/'));
  }

  @UseGuards(JwtAuthGuard)
  @Post()
  async create(@Body() dto: CreatePostDto, @Req() req) {
    this.logger.log(`Intentando crear post. Usuario: ${JSON.stringify(req.user)}, DTO: ${JSON.stringify(dto)}`);
    try {
      return await this.postsService.create(dto, req.user.userId);
    } catch (error) {
      this.logger.error(`Error al crear post: ${error.message}`, error.stack);
      this.logger.error(`Body recibido: ${JSON.stringify(dto)}`);
      throw error;
    }
  }

  // 🌍 PUBLICO
  @Get()
  findAll() {
    return this.postsService.findAll();
  }

  // Obtener un post específico por ID
  @Get(':id')
  async findOne(@Param('id') id: string) {
    return this.postsService.findOne(+id);
  }

  // ❌ BORRADO LOGICO (ADMIN o dueño del post)
  @UseGuards(JwtAuthGuard)
  @Delete(':id')
  async delete(@Param('id') id: string, @Req() req: any) {
    const user = req.user as any;
    const userId = user?.userId || user?.sub;
    return this.postsService.delete(+id, +userId);
  }

  // Obtener posts de un usuario específico
  @Get('user/:userId')
  findByUser(@Param('userId') userId: string) {
    return this.postsService.findByUser(+userId);
  }

}
